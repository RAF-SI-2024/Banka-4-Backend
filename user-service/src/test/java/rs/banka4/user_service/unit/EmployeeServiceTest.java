package rs.banka4.user_service.unit;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.dto.CreateEmployeeDto;
import rs.banka4.user_service.dto.CreateEmployeeResponse;
import rs.banka4.user_service.generator.EmployeeObjectMother;
import rs.banka4.user_service.models.Employee;
import rs.banka4.user_service.models.Privilege;
import rs.banka4.user_service.repositories.EmployeeRepository;
import rs.banka4.user_service.exceptions.DuplicateEmail;
import rs.banka4.user_service.exceptions.DuplicateUsername;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import rs.banka4.user_service.service.impl.EmployeeServiceImpl;


public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

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
        when(employeeRepository.existsByEmail(dto.email())).thenReturn(false);
        when(employeeRepository.existsByUsername(dto.username())).thenReturn(false);

        when(passwordEncoder.encode(dto.password())).thenReturn("encodedPassword");

        when(employeeRepository.save(any())).thenReturn(new Employee());

        ResponseEntity<CreateEmployeeResponse> response = employeeService.createEmployee(dto);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(dto.username(), response.getBody().username());
        assertEquals(dto.email(), response.getBody().email());

        verify(employeeRepository).save(any());
        verify(passwordEncoder).encode(dto.password());
    }
}
