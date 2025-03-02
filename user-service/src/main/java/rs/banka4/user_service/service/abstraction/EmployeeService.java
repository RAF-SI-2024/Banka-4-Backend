package rs.banka4.user_service.service.abstraction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.dto.*;
import rs.banka4.user_service.dto.requests.CreateEmployeeDto;
import rs.banka4.user_service.dto.requests.UpdateEmployeeDto;
import rs.banka4.user_service.models.Employee;

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
