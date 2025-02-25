package rs.banka4.user_service.unit;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.dto.CreateEmployeeDto;
import rs.banka4.user_service.generator.EmployeeObjectMother;
import rs.banka4.user_service.mapper.BasicEmployeeMapper;
import rs.banka4.user_service.models.Employee;
import rs.banka4.user_service.repositories.EmployeeRepository;
import rs.banka4.user_service.exceptions.DuplicateEmail;
import rs.banka4.user_service.exceptions.DuplicateUsername;
import rs.banka4.user_service.service.impl.EmployeeServiceImpl;

public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private BasicEmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldThrowDuplicateEmail_whenEmailAlreadyExists() {
        CreateEmployeeDto dto = EmployeeObjectMother.generateBasicCreateEmployeeDto();
        when(employeeRepository.existsByEmail(dto.email())).thenReturn(true);
        assertThrows(DuplicateEmail.class, () -> employeeService.createEmployee(dto));
    }

    @Test
    void shouldThrowDuplicateUsername_whenUsernameAlreadyExists() {
        CreateEmployeeDto dto = EmployeeObjectMother.generateBasicCreateEmployeeDto();
        when(employeeRepository.existsByUsername(dto.username())).thenReturn(true);
        assertThrows(DuplicateUsername.class, () -> employeeService.createEmployee(dto));
    }

    @Test
    void shouldCreateEmployee_whenValidDataIsProvided() {
        CreateEmployeeDto dto = EmployeeObjectMother.generateBasicCreateEmployeeDto();
        Employee mockEmployee = new Employee();

        when(employeeRepository.existsByEmail(dto.email())).thenReturn(false);
        when(employeeRepository.existsByUsername(dto.username())).thenReturn(false);

        when(employeeMapper.toEntity(dto)).thenReturn(mockEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(mockEmployee);

        ResponseEntity<Void> response = employeeService.createEmployee(dto);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(employeeRepository).save(any(Employee.class));
        verify(employeeMapper).toEntity(dto);
    }
}
