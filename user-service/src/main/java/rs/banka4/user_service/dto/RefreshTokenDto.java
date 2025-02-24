package rs.banka4.user_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RefreshTokenDto (
        @JsonProperty("refresh-token")
        String refreshToken
) {
}
