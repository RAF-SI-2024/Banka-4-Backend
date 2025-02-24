package rs.banka4.user_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RefreshTokenResponseDto(
        @JsonProperty("access_token")
        String accessToken
) {}