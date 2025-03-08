package rs.banka4.user_service.service.abstraction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.domain.auth.dtos.LoginDto;
import rs.banka4.user_service.domain.auth.dtos.LoginResponseDto;
import rs.banka4.user_service.domain.user.PrivilegesDto;
import rs.banka4.user_service.domain.user.employee.dtos.EmployeeDto;
import rs.banka4.user_service.domain.user.employee.dtos.EmployeeResponseDto;
import rs.banka4.user_service.domain.user.employee.dtos.CreateEmployeeDto;
import rs.banka4.user_service.domain.user.employee.dtos.UpdateEmployeeDto;
import rs.banka4.user_service.domain.user.employee.db.Employee;

import java.util.Optional;
import java.util.UUID;

public interface EmployeeService {
    ResponseEntity<LoginResponseDto> login(LoginDto loginDto);
    EmployeeResponseDto getMe(String authorization);
    ResponseEntity<PrivilegesDto> getPrivileges();
    void createEmployee(CreateEmployeeDto dto);
    ResponseEntity<Page<EmployeeDto>> getAll(String firstName, String lastName, String email, String position, PageRequest pageRequest);
    void activateEmployeeAccount(Employee employee, String password);
    Optional<Employee> findEmployeeByEmail(String email);
    void updateEmployee(UUID id, UpdateEmployeeDto updateEmployeeDto);
    EmployeeResponseDto getEmployeeById(UUID id);
}
