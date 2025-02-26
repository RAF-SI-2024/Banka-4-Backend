package rs.banka4.user_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.banka4.user_service.dto.*;
import rs.banka4.user_service.dto.requests.EmployeeVerificationRequestDto;
import rs.banka4.user_service.service.abstraction.AuthService;
import rs.banka4.user_service.service.abstraction.EmployeeService;
import rs.banka4.user_service.service.abstraction.EmployeeVerificationService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmployeeService employeeService;
    private final EmployeeVerificationService employeeVerificationService;

    @PostMapping("/employee/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginDto loginDto) {
        return employeeService.login(loginDto);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponseDto> refreshToken(@RequestBody @Valid RefreshTokenDto refreshTokenDto) {
        return authService.refreshToken(refreshTokenDto.refreshToken());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid LogoutDto logoutDto) {
        return authService.logout(logoutDto);
    }


    @PostMapping("/verify")
    public ResponseEntity<?> verifyEmployeeAccount(@RequestBody EmployeeVerificationRequestDto request) {
        return employeeVerificationService.verifyEmployeeAccount(request);
    }

    @GetMapping("/forgot-password/{email}")
    public ResponseEntity<?> forgotPassword(@PathVariable("email") String email) {
        return employeeVerificationService.forgotPassword(email);
    }
}
