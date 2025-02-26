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
import rs.banka4.user_service.exceptions.NotFound;
import rs.banka4.user_service.exceptions.VerificationCodeExpiredOrInvalid;
import rs.banka4.user_service.exceptions.jwt.RefreshTokenRevoked;
import rs.banka4.user_service.models.Employee;
import rs.banka4.user_service.models.VerificationCode;
import rs.banka4.user_service.repositories.EmployeeRepository;
import rs.banka4.user_service.service.abstraction.EmployeeService;
import rs.banka4.user_service.service.impl.AuthServiceImpl;
import rs.banka4.user_service.service.impl.VerificationCodeService;
import rs.banka4.user_service.utils.JwtUtil;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthUnitTests {

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
    void testLogout() {
        LogoutDto logoutDto = new LogoutDto("some-refresh-token");

        ResponseEntity<Void> response = authService.logout(logoutDto);

        verify(jwtUtil, times(1)).invalidateToken("some-refresh-token");
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testRefreshToken() {
        String token = "some-token";
        String username = "user@example.com";
        Employee employee = new Employee();
        String newAccessToken = "new-access-token";

        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(jwtUtil.isTokenInvalidated(token)).thenReturn(false);
        when(employeeRepository.findByEmail(username)).thenReturn(Optional.of(employee));
        when(jwtUtil.generateToken(employee)).thenReturn(newAccessToken);

        ResponseEntity<RefreshTokenResponseDto> response = authService.refreshToken(token);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(newAccessToken, response.getBody().accessToken());
    }

    @Test
    void testRefreshTokenWithInvalidToken() {
        String token = "some-token";

        when(jwtUtil.isTokenInvalidated(token)).thenReturn(true);

        assertThrows(RefreshTokenRevoked.class, () -> authService.refreshToken(token));
    }

    @Test
    void testVerifyAccountWithInvalidCode() {
        EmployeeVerificationRequestDto request = new EmployeeVerificationRequestDto("invalid-code", "password");

        when(verificationCodeService.validateVerificationCode("invalid-code")).thenReturn(Optional.empty());

        assertThrows(VerificationCodeExpiredOrInvalid.class, () -> authService.verifyAccount(request));
    }

    @Test
    void testForgotPassword() {
        String email = "user@example.com";
        VerificationCode verificationCode = new VerificationCode();
        Employee employee = new Employee();
        employee.setFirstName("John");

        when(verificationCodeService.createVerificationCode(email)).thenReturn(verificationCode);
        when(employeeService.findEmployeeByEmail(email)).thenReturn(Optional.of(employee));

        ResponseEntity<Void> response = authService.forgotPassword(email);

        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), Optional.ofNullable(any()));
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testForgotPasswordWithNonExistentEmail() {
        String email = "nonexistent@example.com";

        when(employeeService.findEmployeeByEmail(email)).thenReturn(Optional.empty());

        assertThrows(NotFound.class, () -> authService.forgotPassword(email));
    }
}