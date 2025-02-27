package rs.banka4.user_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "Login request payload")
public record LoginDto(
        @Schema(description = "Employee email", example = "markovicmarko@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        String email,
        @Schema(description = "Employee password", example = "securepassword", requiredMode = Schema.RequiredMode.REQUIRED)
        String password
) {
}
