package rs.banka4.user_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;


import java.time.LocalDate;
import java.util.List;

@Schema(description = "DTO for employee update")
public record UpdateEmployeeDto(
        @Schema(description = "Employee's first name", example = "Ognjen")
        String firstName,
        @Schema(description = "Employee's last name", example = "Jukic")
        String lastName,
        @Schema(description = "Employee's date of birth", example = "1990-05-15")
        LocalDate dateOfBirth,
        @Schema(description = "Employee's gender", example = "man")
        String gender,
        @Schema(description = "Employee's email address", example = "mljubic9422112rn@raf.rs")
        @Email
        String email,
        @Schema(description = "Employee's username", example = "funfa2c1t")
        String username,
        @Schema(description = "Employee's phone number", example = "+1234567890")
        String phone,
        @Schema(description = "Employee's address", example = "123 Grove Street, City, Country")
        String address,
        @Schema(description = "Employee's privileges", example = "[\"TRADE_STOCKS\", \"CONTRACTS\"]")
        List<String> privilege,
        @Schema(description = "Employee's position", example = "Software Engineer")
        String position,
        @Schema(description = "Employee's department", example = "IT")
        String department,
        @Schema(description = "Indicates if the employee is active", example = "true")
        boolean active
) {
}