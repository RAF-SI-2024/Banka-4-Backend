package rs.banka4.user_service.unit.verification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import rs.banka4.user_service.exceptions.VerificationCodeExpiredOrInvalid;
import rs.banka4.user_service.models.VerificationCode;
import rs.banka4.user_service.repositories.VerificationCodeRepository;
import rs.banka4.user_service.service.impl.VerificationCodeService;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        String code = UUID.randomUUID().toString();
        VerificationCode verificationCode = new VerificationCode(code, LocalDateTime.now().plusDays(7), email);

        when(verificationCodeRepository.save(any(VerificationCode.class))).thenReturn(verificationCode);

        // Act
        VerificationCode createdCode = verificationCodeService.createVerificationCode(email);

        // Assert
        assertNotNull(createdCode);
        assertEquals(email, createdCode.getEmail());
        assertEquals(code, createdCode.getCode());
        verify(verificationCodeRepository, times(1)).save(any(VerificationCode.class));
    }

    @Test
    void testValidateVerificationCodeSuccess() {
        // Arrange
        String code = "valid-code";
        VerificationCode verificationCode = new VerificationCode(code, LocalDateTime.now().plusDays(1), "user@example.com");

        when(verificationCodeRepository.findByCode(code)).thenReturn(Optional.of(verificationCode));

        // Act
        Optional<VerificationCode> result = verificationCodeService.validateVerificationCode(code);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(code, result.get().getCode());
    }

    @Test
    void testValidateVerificationCodeExpired() {
        // Arrange
        String code = "expired-code";
        VerificationCode verificationCode = new VerificationCode(code, LocalDateTime.now().minusDays(1), "user@example.com");

        when(verificationCodeRepository.findByCode(code)).thenReturn(Optional.of(verificationCode));

        // Act & Assert
        assertThrows(VerificationCodeExpiredOrInvalid.class, () -> verificationCodeService.validateVerificationCode(code));
    }

    @Test
    void testValidateVerificationCodeUsed() {
        // Arrange
        String code = "used-code";
        VerificationCode verificationCode = new VerificationCode(code, LocalDateTime.now().plusDays(1), "user@example.com");
        verificationCode.setUsed(true);

        when(verificationCodeRepository.findByCode(code)).thenReturn(Optional.of(verificationCode));

        // Act & Assert
        assertThrows(VerificationCodeExpiredOrInvalid.class, () -> verificationCodeService.validateVerificationCode(code));
    }

    @Test
    void testMarkCodeAsUsed() {
        // Arrange
        String code = "valid-code";
        VerificationCode verificationCode = new VerificationCode(code, LocalDateTime.now().plusDays(1), "user@example.com");

        // Act
        verificationCodeService.markCodeAsUsed(verificationCode);

        // Assert
        assertTrue(verificationCode.isUsed());
        verify(verificationCodeRepository, times(1)).save(verificationCode);
    }

    @Test
    void testFindByEmail() {
        // Arrange
        String email = "user@example.com";
        VerificationCode verificationCode = new VerificationCode("code", LocalDateTime.now().plusDays(1), email);

        when(verificationCodeRepository.findByEmail(email)).thenReturn(Optional.of(verificationCode));

        // Act
        Optional<VerificationCode> result = verificationCodeService.findByEmail(email);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
    }

    @Test
    void testFindByCode() {
        // Arrange
        String code = "valid-code";
        VerificationCode verificationCode = new VerificationCode(code, LocalDateTime.now().plusDays(1), "user@example.com");

        when(verificationCodeRepository.findByCode(code)).thenReturn(Optional.of(verificationCode));

        // Act
        Optional<VerificationCode> result = verificationCodeService.findByCode(code);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(code, result.get().getCode());
    }
}