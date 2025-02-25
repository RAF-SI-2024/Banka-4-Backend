package rs.banka4.user_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.banka4.user_service.dto.CreateEmployeeDto;
import rs.banka4.user_service.dto.CreateEmployeeResponse;
import rs.banka4.user_service.service.abstraction.EmployeeService;

@RestController
@RequestMapping("/employee")
@RequiredArgsConstructor
public class CreateEmployeeController {
    private final EmployeeService employeeService;

    @PostMapping()
    public ResponseEntity<CreateEmployeeResponse> createEmployee(@RequestBody @Valid CreateEmployeeDto createEmployeeDto) {
        return employeeService.createEmployee(createEmployeeDto);
    }
}
