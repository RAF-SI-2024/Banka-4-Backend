//package rs.banka4.user_service.unit.employee;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import rs.banka4.user_service.dto.requests.UpdateEmployeeDto;
//import rs.banka4.user_service.exceptions.DuplicateEmail;
//import rs.banka4.user_service.exceptions.DuplicateUsername;
//import rs.banka4.user_service.exceptions.UserNotFound;
//import rs.banka4.user_service.generator.EmployeeObjectMother;
//import rs.banka4.user_service.models.Employee;
//import rs.banka4.user_service.repositories.EmployeeRepository;
//import rs.banka4.user_service.service.impl.EmployeeServiceImpl;
//import rs.banka4.user_service.mapper.EmployeeMapper;
//
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//public class EmployeeServiceUpdateTests {
//
//    @Mock
//    private EmployeeRepository employeeRepository;
//    @Mock
//    private EmployeeMapper employeeMapper;
//    @InjectMocks
//    private EmployeeServiceImpl employeeService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void testUpdateEmployeeSuccess() {
//        // Arrange
//        String employeeId = "35bc1ef6-f6d0-4405-bcdb-7dc0686b7b87";
//        UpdateEmployeeDto updateEmployeeDto = EmployeeObjectMother.generateBasicUpdateEmployeeDto();
//        Employee employee = new Employee();
//        employee.setId(employeeId);
//
//        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
//        when(employeeRepository.existsByEmail(updateEmployeeDto.email())).thenReturn(false);
//        when(employeeRepository.existsByUsername(updateEmployeeDto.username())).thenReturn(false);
//
//        // Act
//        var response = employeeService.updateEmployee(employeeId, updateEmployeeDto);
//
//        // Assert
//        assertEquals(200, response.getStatusCode().value());
//        verify(employeeMapper, times(1)).updateEmployeeFromDto(updateEmployeeDto, employee);
//        verify(employeeRepository, times(1)).save(employee);
//    }
//
//    @Test
//    void testUpdateEmployeeWithDuplicateEmail() {
//        // Arrange
//        String employeeId = "35bc1ef6-f6d0-4405-bcdb-7dc0686b7b87";
//        UpdateEmployeeDto updateEmployeeDto = EmployeeObjectMother.generateUpdateEmployeeDtoWithDuplicateEmail();
//        Employee employee = new Employee();
//        employee.setId(employeeId);
//
//        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
//        when(employeeRepository.existsByEmail(updateEmployeeDto.email())).thenReturn(true);
//
//        // Act & Assert
//        assertThrows(DuplicateEmail.class, () -> employeeService.updateEmployee(employeeId, updateEmployeeDto));
//    }
//
//    @Test
//    void testUpdateEmployeeWithDuplicateUsername() {
//        // Arrange
//        String employeeId = "35bc1ef6-f6d0-4405-bcdb-7dc0686b7b87";
//        UpdateEmployeeDto updateEmployeeDto = EmployeeObjectMother.generateUpdateEmployeeDtoWithDuplicateUsername();
//        Employee employee = new Employee();
//        employee.setId(employeeId);
//
//        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
//        when(employeeRepository.existsByUsername(updateEmployeeDto.username())).thenReturn(true);
//
//        // Act & Assert
//        assertThrows(DuplicateUsername.class, () -> employeeService.updateEmployee(employeeId, updateEmployeeDto));
//    }
//
//    @Test
//    void testUpdateEmployeeNotFound() {
//        // Arrange
//        String employeeId = "35bc1ef6-f6d0-4405-bcdb-7dc0686b7b87";
//        UpdateEmployeeDto updateEmployeeDto = EmployeeObjectMother.generateUpdateEmployeeDtoWithNonExistentUser();
//
//        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThrows(UserNotFound.class, () -> employeeService.updateEmployee(employeeId, updateEmployeeDto));
//    }
//}