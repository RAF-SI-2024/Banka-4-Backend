package rs.banka4.user_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.banka4.user_service.dto.*;
import rs.banka4.user_service.service.abstraction.EmployeeService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping("/employee/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginDto loginDto) {
        return employeeService.login(loginDto);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponseDto> refreshToken(@RequestBody RefreshTokenDto refreshTokenDto) {
        return employeeService.refreshToken(refreshTokenDto.refreshToken());
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponseDto> me(@RequestHeader("Authorization") String authorization){
        return employeeService.getMe(authorization);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutDto logoutDto, @RequestHeader("Authorization") String authorization) {
        return employeeService.logout(logoutDto);
    }

    @GetMapping("/employee/privileges")
    public ResponseEntity<PrivilegesDto> getPrivileges(@RequestHeader("Authorization") String authorization) {
        return employeeService.getPrivileges();
    }
}
