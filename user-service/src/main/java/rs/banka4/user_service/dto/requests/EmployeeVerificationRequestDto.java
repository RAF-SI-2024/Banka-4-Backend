package rs.banka4.user_service.dto.requests;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Verify employee DTO")
public record EmployeeVerificationRequestDto(
        @NotNull(message = "Password cannot be null")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        @Schema(description = "User's password", example = "password")
        String password,
        @NotNull(message = "Verification code cannot be null")
        @Schema(description = "Verification code", example = "43af5421-d98b-4000-bc99-80762c0d9c56")
        String verificationCode
) {}
