package rs.banka4.user_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.banka4.user_service.controller.docs.EmployeeApiDocumentation;
import rs.banka4.user_service.domain.user.employee.dtos.EmployeeDto;
import rs.banka4.user_service.domain.user.employee.dtos.EmployeeResponseDto;
import rs.banka4.user_service.domain.user.PrivilegesDto;
import rs.banka4.user_service.domain.user.employee.dtos.CreateEmployeeDto;
import rs.banka4.user_service.domain.user.employee.dtos.UpdateEmployeeDto;
import rs.banka4.user_service.service.abstraction.EmployeeService;

import java.util.UUID;

@RestController
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController implements EmployeeApiDocumentation {

    private final EmployeeService employeeService;

    @Override
    @GetMapping("/privileges")
    public ResponseEntity<PrivilegesDto> getPrivileges() {
        return employeeService.getPrivileges();
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<EmployeeResponseDto> me(Authentication auth) {
        return ResponseEntity.ok(employeeService.getMe((String) auth.getCredentials()));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> getEmployeeById(@PathVariable UUID id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @Override
    @PostMapping
    public ResponseEntity<Void> createEmployee(@RequestBody @Valid CreateEmployeeDto createEmployeeDto) {
        employeeService.createEmployee(createEmployeeDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @GetMapping("/search")
    public ResponseEntity<Page<EmployeeDto>> getEmployees(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String position,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return employeeService.getAll(firstName, lastName, email, position, PageRequest.of(page, size));
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateEmployee(@PathVariable UUID id, @RequestBody @Valid UpdateEmployeeDto updateEmployeeDto) {
        employeeService.updateEmployee(id, updateEmployeeDto);
        return ResponseEntity.noContent().build();
    }
}
