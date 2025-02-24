package rs.banka4.user_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LogoutDto(
        @JsonProperty("refresh_token")
        String refreshToken
) {
}
