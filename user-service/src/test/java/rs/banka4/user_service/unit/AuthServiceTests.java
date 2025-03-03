package rs.banka4.user_service.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.dto.LogoutDto;
import rs.banka4.user_service.dto.RefreshTokenResponseDto;
import rs.banka4.user_service.dto.requests.EmployeeVerificationRequestDto;
import rs.banka4.user_service.exceptions.IncorrectCredentials;
import rs.banka4.user_service.exceptions.NotFound;
import rs.banka4.user_service.exceptions.VerificationCodeExpiredOrInvalid;
import rs.banka4.user_service.exceptions.jwt.RefreshTokenRevoked;
import rs.banka4.user_service.generator.AuthObjectMother;
import rs.banka4.user_service.models.Employee;
import rs.banka4.user_service.models.VerificationCode;
import rs.banka4.user_service.repositories.EmployeeRepository;
import rs.banka4.user_service.service.abstraction.EmployeeService;
import rs.banka4.user_service.service.impl.AuthServiceImpl;
import rs.banka4.user_service.service.impl.VerificationCodeService;
import rs.banka4.user_service.utils.JwtUtil;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthServiceTests {

    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private VerificationCodeService verificationCodeService;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private EmployeeService employeeService;
    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLogoutSuccess() {
        // Arrange
        LogoutDto logoutDto = new LogoutDto("some-refresh-token");

        // Act
        ResponseEntity<Void> response = authService.logout(logoutDto);

        // Assert
        verify(jwtUtil, times(1)).invalidateToken("some-refresh-token");
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testLogoutWithInvalidToken() {
        // Arrange
        LogoutDto logoutDto = new LogoutDto("invalid-refresh-token");

        // Act
        doThrow(new RuntimeException("Invalid token")).when(jwtUtil).invalidateToken("invalid-refresh-token");

        // Assert
        assertThrows(RuntimeException.class, () -> authService.logout(logoutDto));
    }

    @Test
    void testLogoutWithAlreadyInvalidatedToken() {
        // Arrange
        LogoutDto logoutDto = new LogoutDto("already-invalidated-token");

        doThrow(new RefreshTokenRevoked()).when(jwtUtil).invalidateToken("already-invalidated-token");

        // Act & Assert
        assertThrows(RefreshTokenRevoked.class, () -> authService.logout(logoutDto));
    }

    @Test
    void testRefreshTokenSuccess() {
        // Arrange
        String token = "valid-token";
        String username = "user@example.com";
        Employee employee = AuthObjectMother.generateEmployee("John", "Doe", "john.doe@example.com", "Developer");
        String newAccessToken = "new-access-token";

        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(jwtUtil.isTokenInvalidated(token)).thenReturn(false);
        when(jwtUtil.extractRole(token)).thenReturn("employee");
        when(employeeRepository.findByEmail(username)).thenReturn(Optional.of(employee));
        when(jwtUtil.generateToken(employee)).thenReturn(newAccessToken);

        // Act
        ResponseEntity<RefreshTokenResponseDto> response = authService.refreshToken(token);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(newAccessToken, response.getBody().accessToken());
    }

    @Test
    void testRefreshTokenWithNonExistentUser() {
        // Arrange
        String token = "valid-token";
        String username = "nonexistent@example.com";

        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(jwtUtil.extractRole(token)).thenReturn("employee");
        when(jwtUtil.isTokenInvalidated(token)).thenReturn(false);
        when(employeeRepository.findByEmail(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IncorrectCredentials.class, () -> authService.refreshToken(token));
    }


    @Test
    void testRefreshTokenWithInvalidToken() {
        // Arrange
        String token = "some-token";

        when(jwtUtil.isTokenInvalidated(token)).thenReturn(true);

        // Act & Assert
        assertThrows(RefreshTokenRevoked.class, () -> authService.refreshToken(token));
    }

    @Test
    void testVerifyAccountWithValidCode() {
        // Arrange
        EmployeeVerificationRequestDto request = AuthObjectMother.generateEmployeeVerificationRequestDto("password", "valid-code");
        VerificationCode verificationCode = AuthObjectMother.generateVerificationCode("user@example.com", "valid-code", false, LocalDateTime.now().plusDays(1));
        Employee employee = AuthObjectMother.generateEmployee("John", "Doe", "john.doe@example.com", "Developer");

        when(verificationCodeService.validateVerificationCode("valid-code")).thenReturn(Optional.of(verificationCode));
        when(employeeService.findEmployeeByEmail("user@example.com")).thenReturn(Optional.of(employee));

        // Act
        ResponseEntity<Void> response = authService.verifyAccount(request);

        // Assert
        verify(employeeService, times(1)).activateEmployeeAccount(employee, "password");
        verify(verificationCodeService, times(1)).markCodeAsUsed(verificationCode);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testVerifyAccountWithInvalidCode() {
        // Arrange
        EmployeeVerificationRequestDto request = AuthObjectMother.generateEmployeeVerificationRequestDto("password", "invalid-code");

        when(verificationCodeService.validateVerificationCode("invalid-code")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(VerificationCodeExpiredOrInvalid.class, () -> authService.verifyAccount(request));
    }

    @Test
    void testVerifyAccountWithUsedCode() {
        // Arrange
        EmployeeVerificationRequestDto request = AuthObjectMother.generateEmployeeVerificationRequestDto("password", "used-code");

        when(verificationCodeService.validateVerificationCode("used-code")).thenThrow(new VerificationCodeExpiredOrInvalid());

        // Act & Assert
        assertThrows(VerificationCodeExpiredOrInvalid.class, () -> authService.verifyAccount(request));
    }

    @Test
    void testForgotPassword() {
        // Arrange
        String email = "user@example.com";
        VerificationCode verificationCode = AuthObjectMother.generateVerificationCode(email, "some-code", false, LocalDateTime.now().plusDays(1));
        Employee employee = AuthObjectMother.generateEmployee("John", "Doe", "john.doe@example.com", "Developer");

        when(verificationCodeService.createVerificationCode(email)).thenReturn(verificationCode);
        when(employeeService.findEmployeeByEmail(email)).thenReturn(Optional.of(employee));

        // Act
        ResponseEntity<Void> response = authService.forgotPassword(email);

        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), Optional.ofNullable(any()));
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testForgotPasswordWithNonExistentEmail() {
        // Arrange
        String email = "nonexistent@example.com";

        when(employeeService.findEmployeeByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFound.class, () -> authService.forgotPassword(email));
    }

    @Test
    void testForgotPasswordWithMessageSendFailure() {
        // Arrange
        String email = "user@example.com";
        VerificationCode verificationCode = AuthObjectMother.generateVerificationCode(email, "some-code", false, LocalDateTime.now().plusDays(1));
        Employee employee = AuthObjectMother.generateEmployee("John", "Doe", "john.doe@example.com", "Developer");

        when(verificationCodeService.createVerificationCode(email)).thenReturn(verificationCode);
        when(employeeService.findEmployeeByEmail(email)).thenReturn(Optional.of(employee));
        doThrow(new RuntimeException("Message send failure")).when(rabbitTemplate).convertAndSend(anyString(), anyString(), Optional.ofNullable(any()));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authService.forgotPassword(email));
    }
}