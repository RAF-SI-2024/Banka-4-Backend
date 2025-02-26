package rs.banka4.user_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;


import java.time.LocalDate;
import java.util.List;


public record EmployeeUpdateDto(
    @JsonProperty("first_name")
    String firstName,
    @JsonProperty("last_name")
    String lastName,
    @JsonProperty("date_of_birth")
    LocalDate dateOfBirth,
    String gender,
    @Email
    String email,
    String password,
    String username,
    String phone,
    String address,
    List<String> privilege,
    String position,
    String department
) {
}