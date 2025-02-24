package rs.banka4.user_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.banka4.user_service.dto.LoginDto;
import rs.banka4.user_service.service.abstraction.EmployeeService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping("/employee/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginDto loginDto) {
        return employeeService.login(loginDto);
    }

}
