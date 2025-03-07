package rs.banka4.user_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.banka4.user_service.controller.docs.AuthApiDocumentation;
import rs.banka4.user_service.dto.LoginDto;
import rs.banka4.user_service.dto.LoginResponseDto;
import rs.banka4.user_service.dto.LogoutDto;
import rs.banka4.user_service.dto.RefreshTokenDto;
import rs.banka4.user_service.dto.RefreshTokenResponseDto;
import rs.banka4.user_service.dto.requests.UserVerificationRequestDto;
import rs.banka4.user_service.service.abstraction.AuthService;
import rs.banka4.user_service.service.abstraction.ClientService;
import rs.banka4.user_service.service.abstraction.EmployeeService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApiDocumentation {

    private final AuthService authService;
    private final EmployeeService employeeService;
    private final ClientService clientService;

    @Override
    @PostMapping("/employee/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginDto loginDto) {
        return employeeService.login(loginDto);
    }

    @Override
    @PostMapping("/client/login")
    public ResponseEntity<LoginResponseDto> clientLogin(@RequestBody @Valid LoginDto loginDto) {
        return clientService.login(loginDto);
    }

    @Override
    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponseDto> refreshToken(@RequestBody @Valid RefreshTokenDto refreshTokenDto) {
        return authService.refreshToken(refreshTokenDto.refreshToken());
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid LogoutDto logoutDto) {
        return authService.logout(logoutDto);
    }

    @Override
    @PostMapping("/verify")
    public ResponseEntity<Void> verifyAccount(@RequestBody @Valid UserVerificationRequestDto request) {
        return authService.verifyAccount(request);
    }

    @Override
    @PostMapping("/forgot-password/{email}")
    public ResponseEntity<Void> forgotPassword(@PathVariable("email") String email) {
        return authService.forgotPassword(email);
    }
}
