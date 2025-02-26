package rs.banka4.user_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PrivilegesDto(
        @JsonProperty("privileges")
        List<String> privileges
) {
}
