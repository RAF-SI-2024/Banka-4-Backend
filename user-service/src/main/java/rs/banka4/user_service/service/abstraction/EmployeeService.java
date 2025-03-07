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

public interface EmployeeService {
    ResponseEntity<LoginResponseDto> login(LoginDto loginDto);
    ResponseEntity<EmployeeResponseDto> getMe(String authorization);
    ResponseEntity<PrivilegesDto> getPrivileges();
    ResponseEntity<Void> createEmployee(CreateEmployeeDto dto);
    ResponseEntity<Page<EmployeeDto>> getAll(String firstName, String lastName, String email, String position, PageRequest pageRequest);
    void activateEmployeeAccount(Employee employee, String password);
    Optional<Employee> findEmployeeByEmail(String email);
    ResponseEntity<Void> updateEmployee(String id, UpdateEmployeeDto updateEmployeeDto);
    ResponseEntity<EmployeeResponseDto> getEmployee(String id);
}
