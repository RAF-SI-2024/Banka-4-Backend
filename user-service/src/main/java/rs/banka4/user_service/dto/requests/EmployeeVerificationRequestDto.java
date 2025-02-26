package rs.banka4.user_service.dto.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EmployeeVerificationRequestDto(
    @JsonProperty("password")
    String password,
    @JsonProperty("verification_code")
    String verificationCode
) {}
