package rs.banka4.user_service.domain.auth.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
@Schema(description = "Valid refresh token to get access token")
public record RefreshTokenDto (
        @Schema(description = "Valid refresh token", example = "eyJhbGciOiJIUzI1NiJ9...")@NotNull
        String refreshToken
) {
}
