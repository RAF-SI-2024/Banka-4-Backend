package rs.banka4.user_service.unit.verification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import rs.banka4.user_service.domain.auth.db.VerificationCode;
import rs.banka4.user_service.exceptions.user.VerificationCodeExpiredOrInvalid;
import rs.banka4.user_service.generator.VerificationCodeObjectMother;
import rs.banka4.user_service.repositories.VerificationCodeRepository;
import rs.banka4.user_service.service.impl.VerificationCodeService;

public class VerificationServiceTests {

    @Mock
    private VerificationCodeRepository verificationCodeRepository;

    @InjectMocks
    private VerificationCodeService verificationCodeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateVerificationCode() {
        // Arrange
        String email = "user@example.com";
        VerificationCode motherCode =
            VerificationCodeObjectMother.createValidVerificationCode(email);
        when(verificationCodeRepository.save(any(VerificationCode.class))).thenReturn(motherCode);

        // Act
        VerificationCode createdCode = verificationCodeService.createVerificationCode(email);

        // Assert
        assertNotNull(createdCode);
        assertEquals(email, createdCode.getEmail());
        verify(verificationCodeRepository, times(1)).save(any(VerificationCode.class));
    }

    @Test
    void testValidateVerificationCodeSuccess() {
        // Arrange
        String email = "user@example.com";
        VerificationCode validCode =
            VerificationCodeObjectMother.createValidVerificationCode(email);
        String code = validCode.getCode();
        when(verificationCodeRepository.findByCode(code)).thenReturn(Optional.of(validCode));

        // Act
        Optional<VerificationCode> result = verificationCodeService.validateVerificationCode(code);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(
            code,
            result.get()
                .getCode()
        );
    }

    @Test
    void testValidateVerificationCodeExpired() {
        // Arrange
        String email = "user@example.com";
        VerificationCode expiredCode =
            VerificationCodeObjectMother.createExpiredVerificationCode(email);
        String code = expiredCode.getCode();
        when(verificationCodeRepository.findByCode(code)).thenReturn(Optional.of(expiredCode));

        // Act & Assert
        assertThrows(
            VerificationCodeExpiredOrInvalid.class,
            () -> verificationCodeService.validateVerificationCode(code)
        );
    }

    @Test
    void testValidateVerificationCodeUsed() {
        // Arrange
        String email = "user@example.com";
        VerificationCode usedCode = VerificationCodeObjectMother.createUsedVerificationCode(email);
        String code = usedCode.getCode();
        when(verificationCodeRepository.findByCode(code)).thenReturn(Optional.of(usedCode));

        // Act & Assert
        assertThrows(
            VerificationCodeExpiredOrInvalid.class,
            () -> verificationCodeService.validateVerificationCode(code)
        );
    }

    @Test
    void testMarkCodeAsUsed() {
        // Arrange
        String email = "user@example.com";
        VerificationCode validCode =
            VerificationCodeObjectMother.createValidVerificationCode(email);

        // Act
        verificationCodeService.markCodeAsUsed(validCode);

        // Assert
        assertTrue(validCode.isUsed());
        verify(verificationCodeRepository, times(1)).save(validCode);
    }

    @Test
    void testFindByEmail() {
        // Arrange
        String email = "user@example.com";
        VerificationCode validCode =
            VerificationCodeObjectMother.createValidVerificationCode(email);
        when(verificationCodeRepository.findByEmail(email)).thenReturn(Optional.of(validCode));

        // Act
        Optional<VerificationCode> result = verificationCodeService.findByEmail(email);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(
            email,
            result.get()
                .getEmail()
        );
    }

    @Test
    void testFindByCode() {
        // Arrange
        String email = "user@example.com";
        VerificationCode validCode =
            VerificationCodeObjectMother.createValidVerificationCode(email);
        String code = validCode.getCode();
        when(verificationCodeRepository.findByCode(code)).thenReturn(Optional.of(validCode));

        // Act
        Optional<VerificationCode> result = verificationCodeService.findByCode(code);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(
            code,
            result.get()
                .getCode()
        );
    }
}
