package rs.banka4.user_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "DTO for employee logout")
public record LogoutDto(
        @Schema(description = "Valid refresh token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String refreshToken
) {
}
