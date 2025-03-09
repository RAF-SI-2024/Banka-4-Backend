package rs.banka4.user_service.unit.employee;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import rs.banka4.user_service.domain.user.employee.db.Employee;
import rs.banka4.user_service.domain.user.employee.dtos.UpdateEmployeeDto;
import rs.banka4.user_service.exceptions.user.DuplicateEmail;
import rs.banka4.user_service.exceptions.user.DuplicateUsername;
import rs.banka4.user_service.exceptions.user.UserNotFound;
import rs.banka4.user_service.generator.EmployeeObjectMother;
import rs.banka4.user_service.repositories.EmployeeRepository;
import rs.banka4.user_service.service.impl.EmployeeServiceImpl;
import rs.banka4.user_service.domain.user.employee.mapper.EmployeeMapper;
import rs.banka4.user_service.service.impl.UserService;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EmployeeServiceUpdateTests {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private EmployeeMapper employeeMapper;
    @Mock
    private UserService userService;
    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUpdateEmployeeSuccess() {
        // Arrange
        UUID employeeId = UUID.fromString("35bc1ef6-f6d0-4405-bcdb-7dc0686b7b87");
        UpdateEmployeeDto updateEmployeeDto = EmployeeObjectMother.generateBasicUpdateEmployeeDto();
        Employee employee = EmployeeObjectMother.generateBasicEmployee();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(userService.existsByEmail(updateEmployeeDto.email())).thenReturn(false);
        when(employeeRepository.existsByUsername(updateEmployeeDto.username())).thenReturn(false);
        doNothing().when(employeeMapper).fromUpdate(employee, updateEmployeeDto);

        // Act
        employeeService.updateEmployee(employeeId, updateEmployeeDto);

        // Assert
        verify(employeeRepository, times(1)).save(employee);
        assertEquals(updateEmployeeDto.email(), employee.getEmail());
        assertEquals(updateEmployeeDto.username(), employee.getUsername());
        assertEquals(updateEmployeeDto.firstName(), employee.getFirstName());
        assertEquals(updateEmployeeDto.lastName(), employee.getLastName());
        assertEquals(updateEmployeeDto.phone(), employee.getPhone());
        assertEquals(updateEmployeeDto.address(), employee.getAddress());
        assertEquals(updateEmployeeDto.position(), employee.getPosition());
        assertEquals(updateEmployeeDto.department(), employee.getDepartment());
    }

    @Test
    void testUpdateEmployeeWithDuplicateEmail() {
        // Arrange
        UUID employeeId = UUID.fromString("35bc1ef6-f6d0-4405-bcdb-7dc0686b7b87");
        UpdateEmployeeDto updateEmployeeDto = EmployeeObjectMother.generateUpdateEmployeeDtoWithDuplicateEmail();
        Employee employee = new Employee();
        employee.setId(employeeId);

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(userService.existsByEmail(updateEmployeeDto.email())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateEmail.class, () -> employeeService.updateEmployee(employeeId, updateEmployeeDto));
    }

    @Test
    void testUpdateEmployeeWithDuplicateUsername() {
        // Arrange
        UUID employeeId = UUID.fromString("35bc1ef6-f6d0-4405-bcdb-7dc0686b7b87");
        UpdateEmployeeDto updateEmployeeDto = EmployeeObjectMother.generateUpdateEmployeeDtoWithDuplicateUsername();
        Employee employee = new Employee();
        employee.setId(employeeId);

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByUsername(updateEmployeeDto.username())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateUsername.class, () -> employeeService.updateEmployee(employeeId, updateEmployeeDto));
    }

    @Test
    void testUpdateEmployeeNotFound() {
        // Arrange
        UUID employeeId = UUID.fromString("35bc1ef6-f6d0-4405-bcdb-7dc0686b7b87");
        UpdateEmployeeDto updateEmployeeDto = EmployeeObjectMother.generateUpdateEmployeeDtoWithNonExistentUser();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFound.class, () -> employeeService.updateEmployee(employeeId, updateEmployeeDto));
    }
}