package rs.banka4.user_service.unit.employee;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import rs.banka4.rafeisen.common.dto.EmployeeResponseDto;
import rs.banka4.rafeisen.common.security.AuthenticatedBankUserAuthentication;
import rs.banka4.rafeisen.common.security.AuthenticatedBankUserPrincipal;
import rs.banka4.rafeisen.common.security.Privilege;
import rs.banka4.rafeisen.common.security.UserType;
import rs.banka4.user_service.domain.auth.dtos.LoginDto;
import rs.banka4.user_service.domain.auth.dtos.LoginResponseDto;
import rs.banka4.user_service.domain.user.PrivilegesDto;
import rs.banka4.user_service.domain.user.employee.db.Employee;
import rs.banka4.user_service.exceptions.user.IncorrectCredentials;
import rs.banka4.user_service.exceptions.user.NotAuthenticated;
import rs.banka4.user_service.generator.EmployeeObjectMother;
import rs.banka4.user_service.repositories.EmployeeRepository;
import rs.banka4.user_service.service.abstraction.JwtService;
import rs.banka4.user_service.service.impl.EmployeeServiceImpl;

public class EmployeeServiceTests {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoginSuccess() {
        // Arrange
        LoginDto loginDto = EmployeeObjectMother.generateBasicLoginDto();
        Employee employee = new Employee();
        employee.setEmail("user@example.com");
        employee.setActive(true);
        employee.setPassword("encoded-password");

        when(authenticationManager.authenticate(any())).thenReturn(
            new AuthenticatedBankUserAuthentication(
                new AuthenticatedBankUserPrincipal(
                    UserType.EMPLOYEE,
                    UUID.fromString("c5df725f-9715-462b-ab27-2562c78c10e2")
                ),
                null,
                EnumSet.noneOf(Privilege.class)
            )
        );
        when(employeeRepository.findByEmail("user@example.com")).thenReturn(Optional.of(employee));
        when(jwtService.generateAccessToken(employee)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");

        // Act
        LoginResponseDto response = employeeService.login(loginDto);

        // Assert
        assertNotNull(response);
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
    }

    @Test
    void testLoginWithIncorrectCredentials() {
        // Arrange
        LoginDto loginDto = EmployeeObjectMother.generateLoginDtoWithIncorrectPassword();

        when(authenticationManager.authenticate(any())).thenThrow(
            new BadCredentialsException("Bad credentials")
        );

        // Act & Assert
        assertThrows(IncorrectCredentials.class, () -> employeeService.login(loginDto));
    }

    @Test
    void testLoginWithNonExistentUser() {
        // Arrange
        LoginDto loginDto = EmployeeObjectMother.generateLoginDtoWithNonExistentUser();

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(employeeRepository.findByEmail("nonexistent@example.com")).thenReturn(
            Optional.empty()
        );

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> employeeService.login(loginDto));
    }

    @Test
    void testGetMeSuccess() {
        // Arrange
        String token = "valid-token";
        String email = "user@example.com";
        Employee employee = new Employee();
        employee.setId(UUID.fromString("44e128a5-ac7a-4c9a-be4c-224b6bf81b20"));
        employee.setEmail(email);

        when(jwtService.extractUserId(token)).thenReturn(
            UUID.fromString("44e128a5-ac7a-4c9a-be4c-224b6bf81b20")
        );
        when(jwtService.isTokenExpired(token)).thenReturn(false);
        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));
        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));

        // Act
        EmployeeResponseDto response = employeeService.getMe("Bearer " + token);

        // Assert
        assertNotNull(response);
        assertEquals(email, response.email());
    }

    @Test
    void testGetMeWithExpiredToken() {
        // Arrange
        String token = "expired-token";

        when(jwtService.isTokenExpired(token)).thenReturn(true);

        // Act & Assert
        assertThrows(NotAuthenticated.class, () -> employeeService.getMe("Bearer " + token));
    }

    @Test
    void testGetPrivilegesWithNoPrivileges() {
        Privilege[] emptyPrivileges = new Privilege[0];
        // Mocking static methods - need to use try with resources to ensure that the mocked static
        // method is restored after the test
        try (MockedStatic<Privilege> mockedPrivilege = mockStatic(Privilege.class)) {
            // Arrange
            mockedPrivilege.when(Privilege::values)
                .thenReturn(emptyPrivileges);

            // Act
            var response = employeeService.getPrivileges();

            // Assert
            assertEquals(
                200,
                response.getStatusCode()
                    .value()
            );
            PrivilegesDto privilegesDto = response.getBody();
            assertNotNull(privilegesDto);
            assertTrue(
                privilegesDto.privileges()
                    .isEmpty()
            );
        }
    }
}
