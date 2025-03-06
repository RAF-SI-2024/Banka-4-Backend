package rs.banka4.user_service.unit.employee;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.dto.*;
import rs.banka4.user_service.dto.requests.CreateEmployeeDto;
import rs.banka4.user_service.exceptions.DuplicateEmail;
import rs.banka4.user_service.exceptions.DuplicateUsername;
import rs.banka4.user_service.generator.EmployeeObjectMother;
import rs.banka4.user_service.mapper.BasicEmployeeMapper;
import rs.banka4.user_service.models.Employee;
import rs.banka4.user_service.models.VerificationCode;
import rs.banka4.user_service.repositories.EmployeeRepository;
import rs.banka4.user_service.service.impl.EmployeeServiceImpl;
import rs.banka4.user_service.service.impl.UserService;
import rs.banka4.user_service.service.impl.VerificationCodeService;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

public class EmployeeServiceCreateTests {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private BasicEmployeeMapper basicEmployeeMapper;
    @Mock
    private VerificationCodeService verificationCodeService;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private UserService userService;
    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateEmployeeSuccess() {
        // Arrange
        CreateEmployeeDto dto = EmployeeObjectMother.generateBasicCreateEmployeeDto();
        Employee employee = new Employee();
        employee.setEmail(dto.email());

        when(userService.existsByEmail(dto.email())).thenReturn(false);
        when(employeeRepository.existsByUsername(dto.username())).thenReturn(false);
        when(basicEmployeeMapper.toEntity(dto)).thenReturn(employee);
        when(employeeRepository.save(employee)).thenReturn(employee);

        // Act
        ResponseEntity<Void> response = employeeService.createEmployee(dto);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        verify(employeeRepository, times(1)).save(employee);
        verify(userService, times(1)).sendVerificationEmail(employee.getFirstName(), employee.getEmail());
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

        // Act & Assert
        assertThrows(DuplicateUsername.class, () -> employeeService.createEmployee(dto));
    }

}
