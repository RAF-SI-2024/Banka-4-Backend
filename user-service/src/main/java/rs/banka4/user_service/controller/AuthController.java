package rs.banka4.user_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.banka4.user_service.dto.*;
import rs.banka4.user_service.dto.requests.EmployeeVerificationRequestDto;
import rs.banka4.user_service.service.abstraction.AuthService;
import rs.banka4.user_service.service.abstraction.EmployeeService;
@Tag(name = "AuthController", description = "Endpoints for authentication")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmployeeService employeeService;

    @Operation(
            summary = "Employee Login",
            description = "Allows an employee to log in and receive an access token and refresh token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful login",
                            content = @Content(schema = @Schema(implementation = LoginDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid data"),
                    @ApiResponse(responseCode = "401", description = "Incorrect credentials")
            }
    )
    @PostMapping("/employee/login")
    public ResponseEntity<LoginResponseDto> login(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Employee login credentials", required = true)
                                                      @RequestBody LoginDto loginDto) {
        return employeeService.login(loginDto);
    }

    @Operation(
            summary = "Refresh Access Token",
            description = "Allows an employee to refresh their access token using a valid refresh token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful token refresh",
                            content = @Content(schema = @Schema(implementation = RefreshTokenResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request data"),
                    @ApiResponse(responseCode = "401", description = "Invalid or expired token"),
                    @ApiResponse(responseCode = "403", description = "Refresh token revoked")
            }
    )
    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponseDto> refreshToken(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Refresh token payload", required = true)
            @RequestBody @Valid RefreshTokenDto refreshTokenDto) {
        return authService.refreshToken(refreshTokenDto.refreshToken());
    }
    @Operation(
            summary = "Logout",
            description = "Logs out the user by revoking their refresh token.",
            security = @SecurityRequirement(name = "bearerAuth"), // Requires authentication
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully logged out"),
                    @ApiResponse(responseCode = "400", description = "Invalid request data"),
                    @ApiResponse(responseCode = "403", description = "Refresh token revoked")
            }
    )
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid LogoutDto logoutDto) {
        return authService.logout(logoutDto);
    }

    @Operation(
            summary = "Verify Employee Account",
            description = "Verifies an employee account using a password and a verification code.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully verified account"),
                    @ApiResponse(responseCode = "400", description = "Invalid request data"),
                    @ApiResponse(responseCode = "404", description = "Verification code not found")
            }
    )
    @PostMapping("/verify")
    public ResponseEntity<Void> verifyEmployeeAccount(@RequestBody @Valid EmployeeVerificationRequestDto request) {
        return authService.verifyAccount(request);
    }

    @Operation(
            summary = "Forgot Password",
            description = "Sends a password reset link to the specified email address if it exists in the system.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Password reset email sent successfully"),
                    @ApiResponse(responseCode = "404", description = "Email not found")
            }
    )
    @PostMapping("/forgot-password/{email}")
    public ResponseEntity<Void> forgotPassword(@PathVariable("email") String email) {
        return authService.forgotPassword(email);
    }
}
