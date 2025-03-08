package rs.banka4.user_service.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import rs.banka4.user_service.dto.RegenerateAuthenticatorResponseDto;
import rs.banka4.user_service.exceptions.*;
import rs.banka4.user_service.models.Client;
import rs.banka4.user_service.models.UserTotpSecret;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.repositories.EmployeeRepository;
import rs.banka4.user_service.repositories.UserTotpSecretRepository;
import rs.banka4.user_service.service.impl.TotpService;
import rs.banka4.user_service.utils.JwtUtil;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TotpServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserTotpSecretRepository repository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private Authentication auth;

    @InjectMocks
    private TotpService totpService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void verifyNewAuthenticator_ShouldReturnOk_WhenValidCode() {

        String email = "test@example.com";
        String code = "123456";
        String token = "Bearer validToken";
        UserTotpSecret userTotpSecret = new UserTotpSecret();
        userTotpSecret.setSecret("validSecret");
        userTotpSecret.setIsActive(false);

        when(auth.getCredentials()).thenReturn(token);
        when(jwtUtil.extractUsername(token)).thenReturn(email);
        when(jwtUtil.isTokenExpired(token)).thenReturn(false);
        when(jwtUtil.isTokenInvalidated(token)).thenReturn(false);
        when(repository.findByClient_Email(email)).thenReturn(Optional.of(userTotpSecret));


        assertDoesNotThrow(() -> totpService.verifyNewAuthenticator(auth, code));
        assertTrue(userTotpSecret.getIsActive());
    }

    @Test
    void verifyNewAuthenticator_ShouldThrowNotValidTotpException_WhenInvalidCode() {

        String email = "test@example.com";
        String code = "invalidCode";
        String token = "Bearer validToken";
        UserTotpSecret userTotpSecret = new UserTotpSecret();
        userTotpSecret.setSecret("validSecret");

        when(auth.getCredentials()).thenReturn(token);
        when(jwtUtil.extractUsername(token)).thenReturn(email);
        when(repository.findByClient_Email(email)).thenReturn(Optional.of(userTotpSecret));


        assertThrows(NotValidTotpException.class, () -> totpService.verifyNewAuthenticator(auth, code));
    }

    @Test
    void regenerateSecret_ShouldReturnNewSecret_WhenValid() {

        String email = "test@example.com";
        String token = "Bearer validToken";
        String newSecret = "newSecret123";
        Client client = new Client();
        client.setEmail(email);

        when(auth.toString()).thenReturn(token);
        when(jwtUtil.extractUsername(token)).thenReturn(email);
        when(jwtUtil.isTokenExpired(token)).thenReturn(false);
        when(jwtUtil.isTokenInvalidated(token)).thenReturn(false);
        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));

        UserTotpSecret userTotpSecret = new UserTotpSecret();
        userTotpSecret.setClient(client);

        when(repository.findByClient_Email(email)).thenReturn(Optional.empty());
        when(repository.save(any(UserTotpSecret.class))).thenReturn(userTotpSecret);


        ResponseEntity<RegenerateAuthenticatorResponseDto> response = totpService.regenerateSecret(auth);


        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(newSecret, response.getBody().tokenSecret());
    }

    @Test
    void regenerateSecret_ShouldThrowNotFound_WhenClientNotFound() {

        String email = "nonexistent@example.com";
        String token = "Bearer validToken";

        when(auth.toString()).thenReturn(token);
        when(jwtUtil.extractUsername(token)).thenReturn(email);
        when(jwtUtil.isTokenExpired(token)).thenReturn(false);
        when(jwtUtil.isTokenInvalidated(token)).thenReturn(false);
        when(clientRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(NotFound.class, () -> totpService.regenerateSecret(auth));
    }

    @Test
    void validate_ShouldReturnTrue_WhenValidCode() {
        String email = "test@example.com";
        String code = "123456";
        String token = "Bearer validToken";
        UserTotpSecret userTotpSecret = new UserTotpSecret();
        userTotpSecret.setSecret("validSecret");
        userTotpSecret.setIsActive(true);

        when(auth.getCredentials()).thenReturn(token);
        when(jwtUtil.extractUsername(token)).thenReturn(email);
        when(jwtUtil.isTokenExpired(token)).thenReturn(false);
        when(jwtUtil.isTokenInvalidated(token)).thenReturn(false);
        when(repository.findByClient_Email(email)).thenReturn(Optional.of(userTotpSecret));


        boolean isValid = totpService.validate(token, code);


        assertTrue(isValid);
    }

    @Test
    void validate_ShouldThrowNotActiveTotpException_WhenTotpIsNotActive() {
        String email = "test@example.com";
        String code = "123456";
        String token = "Bearer validToken";
        UserTotpSecret userTotpSecret = new UserTotpSecret();
        userTotpSecret.setSecret("validSecret");
        userTotpSecret.setIsActive(false);

        when(auth.getCredentials()).thenReturn(token);
        when(jwtUtil.extractUsername(token)).thenReturn(email);
        when(repository.findByClient_Email(email)).thenReturn(Optional.of(userTotpSecret));

        assertThrows(NotActiveTotpException.class, () -> totpService.validate(token, code));
    }

    @Test
    void generateCode_ShouldReturnValidCode_WhenValidToken() {
        String email = "test@example.com";
        String token = "Bearer validToken";
        UserTotpSecret userTotpSecret = new UserTotpSecret();
        userTotpSecret.setSecret("validSecret");

        when(auth.getCredentials()).thenReturn(token);
        when(jwtUtil.extractUsername(token)).thenReturn(email);
        when(jwtUtil.isTokenExpired(token)).thenReturn(false);
        when(jwtUtil.isTokenInvalidated(token)).thenReturn(false);
        when(repository.findByClient_Email(email)).thenReturn(Optional.of(userTotpSecret));


        String generatedCode = totpService.generateCode(token);

        assertNotNull(generatedCode);
    }
}
