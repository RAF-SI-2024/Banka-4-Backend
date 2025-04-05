package rs.banka4.user_service.unit.employee;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import rs.banka4.rafeisen.common.security.AuthenticatedBankUserAuthentication;
import rs.banka4.rafeisen.common.security.Privilege;
import rs.banka4.user_service.domain.user.employee.db.Employee;
import rs.banka4.user_service.domain.user.employee.dtos.CreateEmployeeDto;
import rs.banka4.user_service.domain.user.employee.mapper.EmployeeMapper;
import rs.banka4.user_service.exceptions.user.DuplicateEmail;
import rs.banka4.user_service.exceptions.user.DuplicateUsername;
import rs.banka4.user_service.generator.EmployeeObjectMother;
import rs.banka4.user_service.repositories.EmployeeRepository;
import rs.banka4.user_service.service.abstraction.JwtService;
import rs.banka4.user_service.service.impl.EmployeeServiceImpl;
import rs.banka4.user_service.service.impl.UserService;

public class EmployeeServiceCreateTests {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private EmployeeMapper employeeMapper;
    @Mock
    private UserService userService;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock the security context
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(AuthenticatedBankUserAuthentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("admin@example.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Mock the logged-in employee
        Employee admin = new Employee();
        admin.setEmail("admin@example.com");
        admin.setPrivileges(Set.of(Privilege.ADMIN));
        when(employeeRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
    }

    @Test
    void testCreateEmployeeSuccess() {
        // Arrange
        CreateEmployeeDto dto = EmployeeObjectMother.generateBasicCreateEmployeeDto();
        Employee employee = EmployeeObjectMother.generateEmployeeWithAllAttributes();

        when(userService.existsByEmail(dto.email())).thenReturn(false);
        when(employeeRepository.existsByUsername(dto.username())).thenReturn(false);
        when(employeeMapper.toEntity(dto)).thenReturn(employee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(userService.isPhoneNumberValid(dto.phone())).thenReturn(true);
        when(jwtService.generateAccessToken(any(Employee.class))).thenReturn("mockedToken");

        // Act
        employeeService.createEmployee(dto);

        // Assert
        verify(employeeRepository, times(1)).existsByUsername(dto.username());
        verify(employeeRepository, times(1)).save(
            argThat(
                savedEmployee -> savedEmployee.getEmail()
                    .equals(dto.email())
                    && savedEmployee.getUsername()
                        .equals(dto.username())
            )
        );
        verify(userService, times(1)).sendVerificationEmail(
            employee.getFirstName(),
            employee.getEmail()
        );
    }

    @Test
    void testCreateEmployeeWithDuplicateEmail() {
        // Arrange
        CreateEmployeeDto dto = EmployeeObjectMother.generateBasicCreateEmployeeDto();
        when(userService.existsByEmail(dto.email())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateEmail.class, () -> employeeService.createEmployee(dto));
    }

    @Test
    void testCreateEmployeeWithDuplicateUsername() {
        // Arrange
        CreateEmployeeDto dto = EmployeeObjectMother.generateBasicCreateEmployeeDto();
        when(employeeRepository.existsByUsername(dto.username())).thenReturn(true);
        when(userService.isPhoneNumberValid(dto.phone())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateUsername.class, () -> employeeService.createEmployee(dto));
    }
}
