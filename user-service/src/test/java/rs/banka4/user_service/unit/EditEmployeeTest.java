package rs.banka4.user_service.unit;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import rs.banka4.user_service.dto.requests.UpdateEmployeeDto;
import rs.banka4.user_service.mapper.EmployeeMapper;
import rs.banka4.user_service.models.Employee;
import rs.banka4.user_service.models.Privilege;
import rs.banka4.user_service.repositories.EmployeeRepository;
import rs.banka4.user_service.exceptions.DuplicateUsername;
import rs.banka4.user_service.exceptions.UserNotFound;
import rs.banka4.user_service.exceptions.DuplicateEmail;
import rs.banka4.user_service.service.impl.EmployeeServiceImpl;

public class EditEmployeeTest {
    @Mock
    private EmployeeRepository employeeRepository;

    @Spy
    private EmployeeMapper employeeMapper = Mappers.getMapper(EmployeeMapper.class);

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee existingEmployee;
    private UpdateEmployeeDto updateEmployeeDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        existingEmployee = new Employee();
        existingEmployee.setId("123");
        existingEmployee.setEmail("old.email@example.com");
        existingEmployee.setUsername("oldUsername");
        existingEmployee.setPassword("encodedOldPassword");
        existingEmployee.setPrivileges(EnumSet.of(Privilege.SEARCH));

        updateEmployeeDto = new UpdateEmployeeDto("jon", "jonon",
                LocalDate.now(), "Male", "new.email@example.com", "newUsername",
                "+12324335", "abc 12", List.of("SEARCH","FILTER"),"Nz","12", true);

    }

    @Test
    void shouldUpdateEmployeeSuccessfully() {
        when(employeeRepository.findById("123")).thenReturn(Optional.of(existingEmployee));

        when(employeeRepository.existsByEmail(updateEmployeeDto.email())).thenReturn(false);
        when(employeeRepository.existsByUsername(updateEmployeeDto.username())).thenReturn(false);

        employeeService.updateEmployee("123", updateEmployeeDto);

        verify(employeeMapper).updateEmployeeFromDto(updateEmployeeDto, existingEmployee);

        assertEquals("new.email@example.com", existingEmployee.getEmail());
        assertEquals(EnumSet.of(Privilege.SEARCH, Privilege.FILTER), existingEmployee.getPrivileges());

        verify(employeeRepository).save(existingEmployee);
    }


    @Test
    void shouldThrowExceptionWhenEmployeeNotFound() {
        when(employeeRepository.findById("123")).thenReturn(Optional.empty());

        assertThrows(UserNotFound.class, () -> {
            employeeService.updateEmployee("123", updateEmployeeDto);
        });
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        when(employeeRepository.findById("123")).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.existsByEmail(updateEmployeeDto.email())).thenReturn(true);

        assertThrows(DuplicateEmail.class, () -> {
            employeeService.updateEmployee("123", updateEmployeeDto);
        });
    }

    @Test
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        when(employeeRepository.findById("123")).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.existsByUsername(updateEmployeeDto.username())).thenReturn(true);

        assertThrows(DuplicateUsername.class, () -> {
            employeeService.updateEmployee("123", updateEmployeeDto);
        });
    }


}
