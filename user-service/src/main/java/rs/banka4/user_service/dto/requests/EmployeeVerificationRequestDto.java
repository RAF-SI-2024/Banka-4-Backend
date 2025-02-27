package rs.banka4.user_service.dto.requests;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EmployeeVerificationRequestDto(
        @NotNull(message = "Password cannot be null")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password,
        @NotNull(message = "Verification code cannot be null")
        String verificationCode
) {}
