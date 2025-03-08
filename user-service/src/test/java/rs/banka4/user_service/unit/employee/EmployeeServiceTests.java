//package rs.banka4.user_service.unit.employee;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockedStatic;
//import org.mockito.MockitoAnnotations;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import rs.banka4.user_service.domain.auth.dtos.LoginDto;
//import rs.banka4.user_service.domain.auth.dtos.LoginResponseDto;
//import rs.banka4.user_service.domain.user.PrivilegesDto;
//import rs.banka4.user_service.domain.user.employee.dtos.EmployeeResponseDto;
//import rs.banka4.user_service.exceptions.IncorrectCredentials;
//import rs.banka4.user_service.exceptions.NotAuthenticated;
//import rs.banka4.user_service.generator.EmployeeObjectMother;
//import rs.banka4.user_service.domain.user.employee.db.Employee;
//import rs.banka4.user_service.domain.user.Privilege;
//import rs.banka4.user_service.repositories.EmployeeRepository;
//import rs.banka4.user_service.service.impl.CustomUserDetailsService;
//import rs.banka4.user_service.service.impl.EmployeeServiceImpl;
//import rs.banka4.user_service.utils.JwtUtil;
//
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//public class EmployeeServiceTests {
//
//    @Mock
//    private AuthenticationManager authenticationManager;
//    @Mock
//    private CustomUserDetailsService userDetailsService;
//    @Mock
//    private EmployeeRepository employeeRepository;
//    @Mock
//    private JwtUtil jwtUtil;
//    @InjectMocks
//    private EmployeeServiceImpl employeeService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void testLoginSuccess() {
//        // Arrange
//        LoginDto loginDto = EmployeeObjectMother.generateBasicLoginDto();
//        Employee employee = new Employee();
//        employee.setEmail("user@example.com");
//        employee.setActive(true);
//        employee.setPassword("encoded-password");
//
//        when(authenticationManager.authenticate(any())).thenReturn(null);
//        when(employeeRepository.findByEmail("user@example.com")).thenReturn(Optional.of(employee));
//        when(jwtUtil.generateToken(employee)).thenReturn("access-token");
//        when(jwtUtil.generateRefreshToken(any(), any())).thenReturn("refresh-token");
//        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(null);
//
//        // Act
//        ResponseEntity<LoginResponseDto> response = employeeService.login(loginDto);
//
//        // Assert
//        assertEquals(200, response.getStatusCode().value());
//        assertNotNull(response.getBody());
//        assertEquals("access-token", response.getBody().accessToken());
//        assertEquals("refresh-token", response.getBody().refreshToken());
//    }
//
//    @Test
//    void testLoginWithIncorrectCredentials() {
//        // Arrange
//        LoginDto loginDto = EmployeeObjectMother.generateLoginDtoWithIncorrectPassword();
//
//        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));
//
//        // Act & Assert
//        assertThrows(IncorrectCredentials.class, () -> employeeService.login(loginDto));
//    }
//
//    @Test
//    void testLoginWithNonExistentUser() {
//        // Arrange
//        LoginDto loginDto = EmployeeObjectMother.generateLoginDtoWithNonExistentUser();
//
//        when(authenticationManager.authenticate(any())).thenReturn(null);
//        when(employeeRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThrows(UsernameNotFoundException.class, () -> employeeService.login(loginDto));
//    }
//
//    @Test
//    void testGetMeSuccess() {
//        // Arrange
//        String token = "valid-token";
//        String email = "user@example.com";
//        Employee employee = new Employee();
//        employee.setEmail(email);
//
//        when(jwtUtil.extractUsername(token)).thenReturn(email);
//        when(jwtUtil.isTokenExpired(token)).thenReturn(false);
//        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));
//
//        // Act
//        ResponseEntity<EmployeeResponseDto> response = employeeService.getMe("Bearer " + token);
//
//        // Assert
//        assertEquals(200, response.getStatusCode().value());
//        assertNotNull(response.getBody());
//        assertEquals(email, response.getBody().email());
//    }
//
//    @Test
//    void testGetMeWithExpiredToken() {
//        // Arrange
//        String token = "expired-token";
//
//        when(jwtUtil.isTokenExpired(token)).thenReturn(true);
//
//        // Act & Assert
//        assertThrows(NotAuthenticated.class, () -> employeeService.getMe("Bearer " + token));
//    }
//
//    @Test
//    void testGetPrivilegesWithNoPrivileges() {
//        Privilege[] emptyPrivileges = new Privilege[0];
//        // Mocking static methods - need to use try with resources to ensure that the mocked static method is restored after the test
//        try (MockedStatic<Privilege> mockedPrivilege = mockStatic(Privilege.class)) {
//            // Arrange
//            mockedPrivilege.when(Privilege::values).thenReturn(emptyPrivileges);
//
//            // Act
//            var response = employeeService.getPrivileges();
//
//            // Assert
//            assertEquals(200, response.getStatusCode().value());
//            PrivilegesDto privilegesDto = response.getBody();
//            assertNotNull(privilegesDto);
//            assertTrue(privilegesDto.privileges().isEmpty());
//        }
//    }
//}