package rs.banka4.user_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MeResponseDto(
        String id,
        @JsonProperty("first_name")
        String firstName,
        @JsonProperty("last_name")
        String lastName
) {}