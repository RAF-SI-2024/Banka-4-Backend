package rs.banka4.user_service.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "DTO for employee update")
public record UpdateEmployeeDto(
        @NotBlank(message = "First name is required and cannot be blank.")
        @Schema(description = "Employee's first name", example = "Ognjen")
        String firstName,

        @NotBlank(message = "Last name is required and cannot be blank.")
        @Schema(description = "Employee's last name", example = "Jukic")
        String lastName,

        @NotNull(message = "Date of birth is required and cannot be null.")
        @Schema(description = "Employee's date of birth", example = "1990-05-15")
        LocalDate dateOfBirth,

        @NotBlank(message = "Gender is required and cannot be blank.")
        @Schema(description = "Employee's gender", example = "man")
        String gender,

        @Email(message = "Invalid email format.")
        @NotBlank(message = "Email address is required and cannot be blank.")
        @Schema(description = "Employee's email address", example = "mljubic9422112rn@raf.rs")
        String email,

        @NotBlank(message = "Username is required and cannot be blank.")
        @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters.")
        @Schema(description = "Employee's username", example = "funfa2c1t")
        String username,

        @NotBlank(message = "Phone number is required and cannot be blank.")
        @Schema(description = "Employee's phone number", example = "+1234567890")
        String phone,

        @NotBlank(message = "Address is required and cannot be blank.")
        @Schema(description = "Employee's address", example = "123 Grove Street, City, Country")
        String address,

        @NotNull(message = "Privileges cannot be null.")
        @Size(min = 1, message = "At least one privilege is required.")
        @Schema(description = "Employee's privileges", example = "[\"TRADE_STOCKS\", \"CONTRACTS\"]")
        List<String> privilege,

        @NotBlank(message = "Position is required and cannot be blank.")
        @Schema(description = "Employee's position", example = "Software Engineer")
        String position,

        @NotBlank(message = "Department is required and cannot be blank.")
        @Schema(description = "Employee's department", example = "IT")
        String department,

        @Schema(description = "Indicates if the employee is active", example = "true")
        boolean active
) {
}
