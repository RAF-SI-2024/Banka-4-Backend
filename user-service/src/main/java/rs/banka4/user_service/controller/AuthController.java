package rs.banka4.user_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.banka4.user_service.controller.docs.AuthApiDocumentation;
import rs.banka4.user_service.domain.auth.dtos.LoginDto;
import rs.banka4.user_service.domain.auth.dtos.LoginResponseDto;
import rs.banka4.user_service.domain.auth.dtos.LogoutDto;
import rs.banka4.user_service.domain.auth.dtos.RefreshTokenDto;
import rs.banka4.user_service.domain.auth.dtos.RefreshTokenResponseDto;
import rs.banka4.user_service.domain.auth.dtos.UserVerificationRequestDto;
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
        return ResponseEntity.ok(employeeService.login(loginDto));
    }

    @Override
    @PostMapping("/client/login")
    public ResponseEntity<LoginResponseDto> clientLogin(@RequestBody @Valid LoginDto loginDto) {
        return ResponseEntity.ok( clientService.login(loginDto));
    }

    @Override
    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponseDto> refreshToken(@RequestBody @Valid RefreshTokenDto refreshTokenDto) {
        return ResponseEntity.ok(authService.refreshToken(refreshTokenDto.refreshToken()));
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid LogoutDto logoutDto) {
        authService.logout(logoutDto);
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/verify")
    public ResponseEntity<Void> verifyAccount(@RequestBody @Valid UserVerificationRequestDto request) {
        authService.verifyAccount(request);
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/forgot-password/{email}")
    public ResponseEntity<Void> forgotPassword(@PathVariable("email") String email) {
        authService.forgotPassword(email);
        return ResponseEntity.ok().build();
    }
}
