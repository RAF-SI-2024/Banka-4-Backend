package rs.banka4.user_service.unit.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import rs.banka4.user_service.domain.authenticator.db.UserTotpSecret;
import rs.banka4.user_service.domain.authenticator.dtos.RegenerateAuthenticatorResponseDto;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.exceptions.authenticator.NotActiveTotpException;
import rs.banka4.user_service.exceptions.authenticator.NotValidTotpException;
import rs.banka4.user_service.exceptions.user.NotFound;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.repositories.EmployeeRepository;
import rs.banka4.user_service.repositories.UserTotpSecretRepository;
import rs.banka4.user_service.service.impl.TotpServiceImpl;
import rs.banka4.user_service.utils.JwtUtil;

@ExtendWith(MockitoExtension.class)
public class TotpServiceImplTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserTotpSecretRepository repository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private TotpServiceImpl totpService;

    private final String testEmail = "janesmith92@example.com";
    private final String testSecret = "TEST_SECRET_1234567890";

    @BeforeEach
    void setUp() throws Exception {
        // Mock common JWT behavior
        when(jwtUtil.extractUsername(any())).thenReturn(testEmail);
    }

    // ------------------------- verifyNewAuthenticator Tests -------------------------
    @Test
    void verifyNewAuthenticator_validCode_activatesAndSaves() throws Exception {
        // Arrange
        Authentication auth = mock(Authentication.class);
        when(auth.getCredentials()).thenReturn("Bearer validToken");
        UserTotpSecret totpSecret =
            new UserTotpSecret(UUID.randomUUID(), testSecret, null, null, false);
        when(repository.findByClient_Email(testEmail)).thenReturn(Optional.of(totpSecret));

        String validCode = generateCodeForSecret(testSecret);

        // Act
        totpService.verifyNewAuthenticator(auth, validCode);

        // Assert
        assertThat(totpSecret.getIsActive()).isTrue();
        verify(repository).save(totpSecret);
    }

    @Test
    void verifyNewAuthenticator_invalidCode_throwsException() {
        // Arrange
        Authentication auth = mock(Authentication.class);
        when(auth.getCredentials()).thenReturn("Bearer validToken");
        UserTotpSecret totpSecret =
            new UserTotpSecret(UUID.randomUUID(), testSecret, null, null, false);
        when(repository.findByClient_Email(testEmail)).thenReturn(Optional.of(totpSecret));

        // Act & Assert
        assertThatThrownBy(() -> totpService.verifyNewAuthenticator(auth, "INVALID_CODE"))
            .isInstanceOf(NotValidTotpException.class);
        verify(repository, never()).save(any());
    }

    // ------------------------- validate Tests -------------------------
    @Test
    void validate_validCodeAndActive_returnsTrue() throws Exception {
        // Arrange
        UserTotpSecret activeTotp =
            new UserTotpSecret(UUID.randomUUID(), testSecret, null, null, true);
        when(repository.findByClient_Email(testEmail)).thenReturn(Optional.of(activeTotp));
        String validCode = generateCodeForSecret(testSecret);

        // Act & Assert
        assertThat(totpService.validate("Bearer validToken", validCode)).isTrue();
    }

    @Test
    void validate_inactive_throwsException() {
        // Arrange
        UserTotpSecret inactiveTotp =
            new UserTotpSecret(UUID.randomUUID(), testSecret, null, null, false);
        when(repository.findByClient_Email(testEmail)).thenReturn(Optional.of(inactiveTotp));

        // Act & Assert
        assertThatThrownBy(() -> totpService.validate("Bearer token", "anyCode")).isInstanceOf(
            NotActiveTotpException.class
        );
    }

    // ------------------------- regenerateSecret Tests -------------------------
    @Test
    void regenerateSecret_clientExistingSecret_updatesSecret() {
        // Arrange
        Authentication auth = mock(Authentication.class);
        when(auth.getCredentials()).thenReturn("Bearer validToken");
        Client client = new Client();
        UserTotpSecret existingSecret =
            new UserTotpSecret(UUID.randomUUID(), "OLD_SECRET", client, null, true);
        when(clientRepository.findByEmail(testEmail)).thenReturn(Optional.of(client));
        when(repository.findByClient_Email(testEmail)).thenReturn(Optional.of(existingSecret));

        // Act
        RegenerateAuthenticatorResponseDto response = totpService.regenerateSecret(auth);

        // Assert
        assertThat(response.tokenSecret()).hasSize(32); // DefaultSecretGenerator length
        assertThat(existingSecret.getSecret()).isNotEqualTo("OLD_SECRET");
        assertThat(existingSecret.getIsActive()).isFalse();
        verify(repository).save(existingSecret);
    }

    @Test
    void regenerateSecret_noUser_throwsNotFound() {
        // Arrange
        Authentication auth = mock(Authentication.class);
        when(auth.getCredentials()).thenReturn("Bearer validToken");
        when(clientRepository.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> totpService.regenerateSecret(auth)).isInstanceOf(NotFound.class);
    }

    // ------------------------- generateCode Tests -------------------------
    @Test
    void generateCode_validToken_returnsCode() throws Exception {
        // Arrange
        UserTotpSecret totp = new UserTotpSecret(UUID.randomUUID(), testSecret, null, null, true);
        when(repository.findByClient_Email(testEmail)).thenReturn(Optional.of(totp));

        // Act
        String code = totpService.generateCode("Bearer validToken");

        // Assert
        String expectedCode = generateCodeForSecret(testSecret);
        assertThat(code).isEqualTo(expectedCode);
    }

    // ------------------------- Helper Methods -------------------------
    private String generateCodeForSecret(String secret) throws Exception {
        Method method = TotpServiceImpl.class.getDeclaredMethod("generateCodeFromSecret", String.class);
        method.setAccessible(true);
        return (String) method.invoke(totpService, secret);
    }
}
