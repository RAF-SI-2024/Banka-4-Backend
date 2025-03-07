package rs.banka4.user_service.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.dto.LoginDto;
import rs.banka4.user_service.dto.LoginResponseDto;
import rs.banka4.user_service.dto.LogoutDto;
import rs.banka4.user_service.dto.RefreshTokenDto;
import rs.banka4.user_service.dto.RefreshTokenResponseDto;
import rs.banka4.user_service.dto.requests.UserVerificationRequestDto;
import rs.banka4.user_service.exceptions.IncorrectCredentials;
import rs.banka4.user_service.exceptions.UserNotFound;
import rs.banka4.user_service.exceptions.VerificationCodeExpiredOrInvalid;
import rs.banka4.user_service.exceptions.jwt.RefreshTokenRevoked;

@Tag(name = "AuthController", description = "Endpoints for authentication")
public interface AuthApiDocumentation {

    @Operation(
            summary = "Employee Login",
            description = "Allows an employee to log in and receive an access token and refresh token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful login",
                            content = @Content(schema = @Schema(implementation = LoginResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid data"),
                    @ApiResponse(responseCode = "401", description = "Incorrect credentials",
                            content = @Content(schema = @Schema(implementation = IncorrectCredentials.class)))
            }
    )
    ResponseEntity<LoginResponseDto> login(@Valid LoginDto loginDto);

    @Operation(
            summary = "Client Login",
            description = "Allows a client to log in and receive an access token and refresh token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful login",
                            content = @Content(schema = @Schema(implementation = LoginResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid data"),
                    @ApiResponse(responseCode = "401", description = "Incorrect credentials",
                            content = @Content(schema = @Schema(implementation = IncorrectCredentials.class)))
            }
    )
    ResponseEntity<LoginResponseDto> clientLogin(@Valid LoginDto loginDto);

    @Operation(
            summary = "Refresh Access Token",
            description = "Allows an employee to refresh their access token using a valid refresh token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful token refresh",
                            content = @Content(schema = @Schema(implementation = RefreshTokenResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request data"),
                    @ApiResponse(responseCode = "401", description = "Invalid or expired token",
                            content = @Content(schema = @Schema(implementation = VerificationCodeExpiredOrInvalid.class))),
                    @ApiResponse(responseCode = "403", description = "Refresh token revoked",
                            content = @Content(schema = @Schema(implementation = RefreshTokenRevoked.class)))
            }
    )
    ResponseEntity<RefreshTokenResponseDto> refreshToken(@Valid RefreshTokenDto refreshTokenDto);

    @Operation(
            summary = "Logout",
            description = "Logs out the user by revoking their refresh token.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully logged out"),
                    @ApiResponse(responseCode = "400", description = "Invalid request data"),
                    @ApiResponse(responseCode = "403", description = "Refresh token revoked",
                            content = @Content(schema = @Schema(implementation = RefreshTokenRevoked.class)))
            }
    )
    ResponseEntity<Void> logout(@Valid LogoutDto logoutDto);

    @Operation(
            summary = "Verify Employee Account",
            description = "Verifies an employee account using a password and a verification code.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully verified account"),
                    @ApiResponse(responseCode = "400", description = "Invalid request data"),
                    @ApiResponse(responseCode = "404", description = "Verification code expired or user not found",
                            content = @Content(schema = @Schema(implementation = VerificationCodeExpiredOrInvalid.class)))
            }
    )
    ResponseEntity<Void> verifyAccount(@Valid UserVerificationRequestDto request);

    @Operation(
            summary = "Forgot Password",
            description = "Sends a password reset link to the specified email address if it exists in the system.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Password reset email sent successfully"),
                    @ApiResponse(responseCode = "404", description = "User not found",
                            content = @Content(schema = @Schema(implementation = UserNotFound.class)))
            }
    )
    ResponseEntity<Void> forgotPassword(String email);
}
