package rs.banka4.user_service.domain.auth.dtos;


import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "Newly generated JWT access token for user")
public record RefreshTokenResponseDto(
        @Schema(description = "Newly generated JWT access token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken
) {}