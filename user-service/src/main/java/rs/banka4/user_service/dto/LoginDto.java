package rs.banka4.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record LoginDto(
        @Email(message = "Invalid email format")
        String email,
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password
) {
}
