package rs.banka4.user_service.unit.employee;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.dto.EmployeeDto;
import rs.banka4.user_service.generator.EmployeeObjectMother;
import rs.banka4.user_service.models.Employee;
import rs.banka4.user_service.repositories.EmployeeRepository;
import rs.banka4.user_service.service.impl.EmployeeServiceImpl;

import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class EmployeeServiceFilterTests {

    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private static Stream<Arguments> provideFilters() {
        return Stream.of(
                Arguments.of("John", "Doe", "user@example.com", "Developer"),
                Arguments.of("John", null, null, null),
                Arguments.of(null, "Doe", null, null),
                Arguments.of(null, null, "user@example.com", null),
                Arguments.of(null, null, null, "Developer"),
                Arguments.of(null, null, null, null),
                Arguments.of("John", "Do", "user@", "Devel")
        );
    }

    @ParameterizedTest
    @MethodSource("provideFilters")
    void testGetAllEmployeesWithFilters(String firstName, String lastName, String email, String position) {
        // Arrange
        PageRequest pageRequest = PageRequest.of(0, 10);
        Employee employee = EmployeeObjectMother.generateEmployeeWithAllAttributes();
        Page<Employee> employeePage = new PageImpl<>(Collections.singletonList(employee));

        when(employeeRepository.findAll(any(Specification.class), eq(pageRequest))).thenReturn(employeePage);

        // Act
        ResponseEntity<Page<EmployeeDto>> response = employeeService.getAll(firstName, lastName, email, position, pageRequest);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
    }
}