package rs.banka4.user_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.banka4.user_service.dto.LoginDto;
import rs.banka4.user_service.dto.RefreshTokenDto;
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

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenDto refreshTokenDto) {
        return employeeService.refreshToken(refreshTokenDto.refreshToken());
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader("Authorization") String authorization){
        return employeeService.getMe(authorization);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authorization) {
        return employeeService.logout(authorization);
    }

}
