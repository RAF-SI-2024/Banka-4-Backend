package rs.banka4.user_service.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "DTO for creating employee")
public record CreateEmployeeDto(
        @Schema(description = "Employee's first name", example = "Ognjen")
        @NotBlank(message = "First name is required")
        String firstName,

        @Schema(description = "Employee's last name", example = "Jukic")
        @NotBlank(message = "Last name is required")
        String lastName,

        @Schema(description = "Employee's username", example = "funfa2c1t")
        @NotBlank(message = "Username is required")
        String username,

        @Schema(description = "Employee's date of birth", example = "1990-05-15")
        @NotNull(message = "Date of birth is required")
        LocalDate dateOfBirth,

        @Schema(description = "Employee's gender", example = "man")
        @NotBlank(message = "Gender is required")
        String gender,

        @Schema(description = "Employee's email address", example = "mljubic9422112rn@raf.rs")
        @Email(message = "Email should be valid")
        @NotBlank(message = "Email is required")
        String email,

        @Schema(description = "Employee's phone number", example = "+1234567890")
        @NotBlank(message = "Phone is required")
        String phone,

        @Schema(description = "Employee's address", example = "123 Grove Street, City, Country")
        @NotBlank(message = "Address is required")
        String address,

        @Schema(description = "Employee's privileges", example = "[\"TRADE_STOCKS\", \"CONTRACTS\"]")
        @NotNull(message = "Privileges are required")
        List<String> privilege,

        @Schema(description = "Employee's position", example = "Software Engineer")
        @NotBlank(message = "Position is required")
        String position,

        @Schema(description = "Employee's department", example = "IT")
        @NotBlank(message = "Department is required")
        String department,

        @Schema(description = "Indicates if the employee is active", example = "true")
        @NotNull(message = "Active is required")
        boolean active
) {
}
