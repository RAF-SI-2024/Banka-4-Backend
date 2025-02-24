package rs.banka4.user_service.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import rs.banka4.user_service.dto.LoginDto;
import rs.banka4.user_service.exceptions.IncorrectCredentials;
import rs.banka4.user_service.exceptions.NotAuthenticated;
import rs.banka4.user_service.exceptions.RefreshTokenExpired;
import rs.banka4.user_service.models.Employee;
import rs.banka4.user_service.repositories.EmployeeRepository;
import rs.banka4.user_service.service.impl.CustomUserDetailsService;
import rs.banka4.user_service.service.impl.EmployeeServiceImpl;
import rs.banka4.user_service.utils.JwtUtil;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AuthUnitTests {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private CustomUserDetailsService userDetailsService;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testLoginSuccess() {
        // Arrange
        LoginDto loginDto = new LoginDto("test@example.com", "password123");
        Employee employee = new Employee();
        employee.setEmail("test@example.com");
        employee.setPassword("password123");

        when(employeeRepository.findByEmail(loginDto.email())).thenReturn(Optional.of(employee));
        when(jwtUtil.generateToken(any(Employee.class))).thenReturn("access_token");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("refresh_token");

        // Act
        ResponseEntity<?> response = employeeService.login(loginDto);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertInstanceOf(Map.class, response.getBody());

        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("access_token", responseBody.get("access_token"));
        assertEquals("refresh_token", responseBody.get("refresh_token"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(employeeRepository).findByEmail(loginDto.email());
        verify(jwtUtil).generateToken(any(Employee.class));
        verify(jwtUtil).generateRefreshToken(any());
    }

    @Test
    public void testLoginFailure() {
        // Arrange
        LoginDto loginDto = new LoginDto("test@example.com", "wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new IncorrectCredentials());

        // Act & Assert
        assertThrows(IncorrectCredentials.class, () -> employeeService.login(loginDto));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(employeeRepository, never()).findByEmail(anyString());
        verify(jwtUtil, never()).generateToken(any(Employee.class));
        verify(jwtUtil, never()).generateRefreshToken(any());
    }

    @Test
    public void testRefreshToken_Success() {
        // Arrange
        String token = "Bearer validRefreshToken";
        String refreshToken = "validRefreshToken";
        String username = "test@example.com";
        String newAccessToken = "newAccessToken";

        Employee employee = new Employee();
        employee.setEmail(username);

        when(jwtUtil.extractUsername(refreshToken)).thenReturn(username);
        when(jwtUtil.isTokenExpired(refreshToken)).thenReturn(false);
        when(employeeRepository.findByEmail(username)).thenReturn(Optional.of(employee));
        when(jwtUtil.generateToken(employee)).thenReturn(newAccessToken);

        // Act
        ResponseEntity<?> response = employeeService.refreshToken(token);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(newAccessToken, ((Map<String, String>) response.getBody()).get("access_token"));
    }

    @Test
    public void testRefreshToken_InvalidTokenFormat() {
        // Arrange
        String token = "invalidToken";

        // Act & Assert
        assertThrows(IncorrectCredentials.class, () -> employeeService.refreshToken(token));
    }

    @Test
    public void testRefreshToken_TokenExpired() {
        // Arrange
        String token = "Bearer expiredRefreshToken";
        String refreshToken = "expiredRefreshToken";
        String username = "test@example.com";

        when(jwtUtil.extractUsername(refreshToken)).thenReturn(username);
        when(jwtUtil.isTokenExpired(refreshToken)).thenReturn(true);

        // Act & Assert
        assertThrows(RefreshTokenExpired.class, () -> employeeService.refreshToken(token));
    }

    @Test
    public void testRefreshToken_EmployeeNotFound() {
        // Arrange
        String token = "Bearer validRefreshToken";
        String refreshToken = "validRefreshToken";
        String username = "test@example.com";

        when(jwtUtil.extractUsername(refreshToken)).thenReturn(username);
        when(jwtUtil.isTokenExpired(refreshToken)).thenReturn(false);
        when(employeeRepository.findByEmail(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IncorrectCredentials.class, () -> employeeService.refreshToken(token));
    }

    @Test
    public void testGetMe_Success() {
        // Arrange
        String token = "Bearer validToken";
        String username = "test@example.com";
        Employee employee = new Employee();
        employee.setId("1");
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setEmail(username);

        when(jwtUtil.extractUsername(anyString())).thenReturn(username);
        when(jwtUtil.isTokenExpired(anyString())).thenReturn(false);
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.of(employee));

        // Act
        ResponseEntity<?> response = employeeService.getMe(token);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertInstanceOf(Map.class, response.getBody());

        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("1", responseBody.get("id"));
        assertEquals("John", responseBody.get("first_name"));
        assertEquals("Doe", responseBody.get("last_name"));

        verify(jwtUtil).extractUsername(anyString());
        verify(jwtUtil).isTokenExpired(anyString());
        verify(employeeRepository).findByEmail(anyString());
    }

    @Test
    public void testGetMe_MissingAuthorizationHeader() {
        // Act & Assert
        assertThrows(NotAuthenticated.class, () -> employeeService.getMe(null));
    }

    @Test
    public void testGetMe_InvalidTokenFormat() {
        // Act & Assert
        assertThrows(NotAuthenticated.class, () -> employeeService.getMe("invalidToken"));
    }

    @Test
    public void testGetMe_TokenExpired() {
        // Arrange
        String token = "Bearer expiredToken";
        String username = "test@example.com";

        when(jwtUtil.extractUsername(anyString())).thenReturn(username);
        when(jwtUtil.isTokenExpired(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(NotAuthenticated.class, () -> employeeService.getMe(token));

        verify(jwtUtil).extractUsername(anyString());
        verify(jwtUtil).isTokenExpired(anyString());
        verify(employeeRepository, never()).findByEmail(anyString());
    }

    @Test
    public void testGetMe_EmployeeNotFound() {
        // Arrange
        String token = "Bearer validToken";
        String username = "test@example.com";

        when(jwtUtil.extractUsername(anyString())).thenReturn(username);
        when(jwtUtil.isTokenExpired(anyString())).thenReturn(false);
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotAuthenticated.class, () -> employeeService.getMe(token));

        verify(jwtUtil).extractUsername(anyString());
        verify(jwtUtil).isTokenExpired(anyString());
        verify(employeeRepository).findByEmail(anyString());
    }

    @Test
    public void testLogout_Success() {
        // Arrange
        String token = "Bearer validToken";

        // Act
        ResponseEntity<?> response = employeeService.logout(token);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertInstanceOf(Map.class, response.getBody());

        verify(jwtUtil).invalidateToken("validToken");
    }

    @Test
    public void testLogout_MissingAuthorizationHeader() {
        // Act & Assert
        assertThrows(NotAuthenticated.class, () -> employeeService.logout(null));
    }

    @Test
    public void testLogout_InvalidTokenFormat() {
        // Act & Assert
        assertThrows(NotAuthenticated.class, () -> employeeService.logout("invalidToken"));
    }

}
