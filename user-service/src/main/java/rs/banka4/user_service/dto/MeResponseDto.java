package rs.banka4.user_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import rs.banka4.user_service.models.Privilege;

import java.time.LocalDate;
import java.util.EnumSet;

public record MeResponseDto(
        String id,
        @JsonProperty("first_name")
        String firstName,
        @JsonProperty("last_name")
        String lastName,
        @JsonProperty("date_of_birth")
        LocalDate dateOfBirth,
        String gender,
        String email,
        String phone,
        String address,
        String username,
        String position,
        String department,
        EnumSet<Privilege> privileges
) {}