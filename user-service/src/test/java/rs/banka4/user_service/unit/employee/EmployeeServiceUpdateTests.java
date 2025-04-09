package rs.banka4.user_service.unit.employee;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import rs.banka4.rafeisen.common.security.AuthenticatedBankUserAuthentication;
import rs.banka4.rafeisen.common.security.AuthenticatedBankUserPrincipal;
import rs.banka4.rafeisen.common.security.Privilege;
import rs.banka4.rafeisen.common.security.UserType;
import rs.banka4.user_service.domain.user.employee.db.Employee;
import rs.banka4.user_service.domain.user.employee.dtos.UpdateEmployeeDto;
import rs.banka4.user_service.domain.user.employee.mapper.EmployeeMapper;
import rs.banka4.user_service.exceptions.user.DuplicateEmail;
import rs.banka4.user_service.exceptions.user.DuplicateUsername;
import rs.banka4.user_service.exceptions.user.UserNotFound;
import rs.banka4.user_service.generator.EmployeeObjectMother;
import rs.banka4.user_service.repositories.EmployeeRepository;
import rs.banka4.user_service.service.impl.EmployeeServiceImpl;
import rs.banka4.user_service.service.impl.UserService;

public class EmployeeServiceUpdateTests {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private UUID adminId;
    private String adminEmail = "admin@example.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        adminId = UUID.randomUUID();
        AuthenticatedBankUserPrincipal principal =
            new AuthenticatedBankUserPrincipal(UserType.EMPLOYEE, adminId);

        Authentication authentication = mock(AuthenticatedBankUserAuthentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(authentication.getName()).thenReturn(adminEmail);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Employee admin = new Employee();
        admin.setId(adminId);
        admin.setEmail(adminEmail);
        admin.setPrivileges(Set.of(Privilege.ADMIN));
        when(employeeRepository.findById(adminId)).thenReturn(Optional.of(admin));
    }

    @Test
    void testUpdateEmployeeSuccess() {
        UUID employeeId = UUID.fromString("35bc1ef6-f6d0-4405-bcdb-7dc0686b7b87");
        UpdateEmployeeDto updateEmployeeDto = EmployeeObjectMother.generateBasicUpdateEmployeeDto();
        Employee employee = EmployeeObjectMother.generateBasicEmployee();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(userService.existsByEmail(updateEmployeeDto.email())).thenReturn(false);
        when(userService.isPhoneNumberValid(updateEmployeeDto.phoneNumber())).thenReturn(true);
        when(employeeRepository.existsByUsername(updateEmployeeDto.username())).thenReturn(false);
        doNothing().when(employeeMapper)
            .fromUpdate(employee, updateEmployeeDto);

        employeeService.updateEmployee(employeeId, updateEmployeeDto);

        verify(employeeRepository, times(1)).save(employee);
        assertEquals(updateEmployeeDto.email(), employee.getEmail());
        assertEquals(updateEmployeeDto.username(), employee.getUsername());
        assertEquals(updateEmployeeDto.firstName(), employee.getFirstName());
        assertEquals(updateEmployeeDto.lastName(), employee.getLastName());
        assertEquals(updateEmployeeDto.phoneNumber(), employee.getPhone());
        assertEquals(updateEmployeeDto.address(), employee.getAddress());
        assertEquals(updateEmployeeDto.position(), employee.getPosition());
        assertEquals(updateEmployeeDto.department(), employee.getDepartment());
    }

    @Test
    void testUpdateEmployeeWithDuplicateEmail() {
        UUID employeeId = UUID.fromString("35bc1ef6-f6d0-4405-bcdb-7dc0686b7b87");
        UpdateEmployeeDto updateEmployeeDto =
            EmployeeObjectMother.generateUpdateEmployeeDtoWithDuplicateEmail();
        Employee employee = new Employee();
        employee.setId(employeeId);

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(userService.existsByEmail(updateEmployeeDto.email())).thenReturn(true);

        assertThrows(
            DuplicateEmail.class,
            () -> employeeService.updateEmployee(employeeId, updateEmployeeDto)
        );
    }

    @Test
    void testUpdateEmployeeWithDuplicateUsername() {
        UUID employeeId = UUID.fromString("35bc1ef6-f6d0-4405-bcdb-7dc0686b7b87");
        UpdateEmployeeDto updateEmployeeDto =
            EmployeeObjectMother.generateUpdateEmployeeDtoWithDuplicateUsername();
        Employee employee = new Employee();
        employee.setId(employeeId);

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByUsername(updateEmployeeDto.username())).thenReturn(true);
        when(userService.isPhoneNumberValid(updateEmployeeDto.phoneNumber())).thenReturn(true);

        assertThrows(
            DuplicateUsername.class,
            () -> employeeService.updateEmployee(employeeId, updateEmployeeDto)
        );
    }

    @Test
    void testUpdateEmployeeNotFound() {
        UUID employeeId = UUID.fromString("35bc1ef6-f6d0-4405-bcdb-7dc0686b7b87");
        UpdateEmployeeDto updateEmployeeDto =
            EmployeeObjectMother.generateUpdateEmployeeDtoWithNonExistentUser();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        assertThrows(
            UserNotFound.class,
            () -> employeeService.updateEmployee(employeeId, updateEmployeeDto)
        );
    }
}
