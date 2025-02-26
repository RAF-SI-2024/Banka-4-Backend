package rs.banka4.user_service.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.dto.EmployeeDto;
import rs.banka4.user_service.mapper.EmployeeMapper;
import rs.banka4.user_service.models.Employee;
import rs.banka4.user_service.repositories.EmployeeRepository;
import rs.banka4.user_service.service.impl.EmployeeServiceImpl;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class EmployeeServiceTests {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @Mock
    private EmployeeMapper employeeMapper;

    @Mock
    private PageRequest pageRequest;

    private Employee employee;

    private EmployeeDto employeeDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Prepare mock data
        employee = new Employee(
                "1", "John", "Doe", LocalDate.of(1990, 1, 1), "Male",
                "john.doe@example.com", "123-456-7890", "123 Street",
                "password", "john.doe@example.com", "Developer",
                "IT", true, 0L
        );

        employeeDto = new EmployeeDto(
                "1", "John", "Doe", LocalDate.of(1990, 1, 1), "Male",
                "john.doe@example.com", "123-456-7890", "123 Street",
                "john.doe@example.com", "Developer", "IT"
        );
    }

    @Test
    void testGetAllWithFilters() {
        // Prepare mock data
        Page<Employee> employeePage = new PageImpl<>(List.of(employee), PageRequest.of(0, 10), 1);
        Page<EmployeeDto> employeeDtoPage = new PageImpl<>(List.of(employeeDto), PageRequest.of(0, 10), 1);

        // Mock repository call
        when(employeeRepository.findAll(any(Specification.class), eq(pageRequest))).thenReturn(employeePage);

        // Call method under test
        ResponseEntity<Page<EmployeeDto>> response = employeeService.getAll("John", "Doe", "john.doe@example.com", "Developer", pageRequest);

        // Assertions
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals("John", response.getBody().getContent().get(0).firstName());
        assertEquals("Doe", response.getBody().getContent().get(0).lastName());
    }

    @Test
    void testGetAllWithNoFilters() {
        // Prepare mock data
        Page<Employee> employeePage = new PageImpl<>(List.of(employee), PageRequest.of(0, 10), 1);

        // Mock repository call with no filters
        when(employeeRepository.findAll(any(Specification.class), eq(pageRequest))).thenReturn(employeePage);

        // Call method under test with no filters
        ResponseEntity<Page<EmployeeDto>> response = employeeService.getAll(null, null, null, null, pageRequest);

        // Assertions
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals("John", response.getBody().getContent().get(0).firstName());
    }

    @Test
    void testGetAllWithInvalidFilter() {
        // Prepare mock data
        Page<Employee> employeePage = new PageImpl<>(List.of(employee), PageRequest.of(0, 10), 1);

        // Mock repository call with invalid filter
        when(employeeRepository.findAll(any(Specification.class), eq(pageRequest))).thenReturn(employeePage);

        // Call method under test with invalid filter
        ResponseEntity<Page<EmployeeDto>> response = employeeService.getAll("Invalid", "Name", "invalid@example.com", "Unknown", pageRequest);

        // Assertions
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetAllWithEmptyPage() {
        // Mock an empty page
        Page<Employee> employeePage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        // Mock repository call with no data
        when(employeeRepository.findAll(any(Specification.class), eq(pageRequest))).thenReturn(employeePage);

        // Call method under test with no results
        ResponseEntity<Page<EmployeeDto>> response = employeeService.getAll(null, null, null, null, pageRequest);

        // Assertions
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getContent().isEmpty());
    }

}
