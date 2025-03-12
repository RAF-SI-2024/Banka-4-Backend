package rs.banka4.user_service.domain.user.employee.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.Set;
import rs.banka4.user_service.domain.user.Gender;
import rs.banka4.user_service.domain.user.Privilege;

@Schema(description = "DTO for employee update")
public record UpdateEmployeeDto(

    @Schema(
        description = "Employee's first name",
        example = "Ognjen"
    ) String firstName,

    @Schema(
        description = "Employee's last name",
        example = "Jukic"
    ) String lastName,

    @Schema(
        description = "Employee's date of birth",
        example = "1990-05-15"
    ) LocalDate dateOfBirth,

    @Schema(
        description = "Gender",
        example = "MALE"
    ) Gender gender,

    @Email(message = "Invalid email format.")
    @Schema(
        description = "Employee's email address",
        example = "mljubic9422112rn@raf.rs"
    ) String email,

    @Size(
        min = 3,
        max = 20,
        message = "Username must be between 3 and 20 characters."
    )
    @Schema(
        description = "Employee's username",
        example = "funfa2c1t"
    ) String username,

    @Schema(
        description = "Employee's phoneNumber number",
        example = "+1234567890"
    ) String phoneNumber,

    @Schema(
        description = "Employee's address",
        example = "123 Grove Street, City, Country"
    ) String address,

    @Schema(
        description = "Employee's privileges",
        example = "[\"TRADE_STOCKS\", \"CONTRACTS\"]"
    ) Set<Privilege> privilege,

    @Schema(
        description = "Employee's position",
        example = "Software Engineer"
    ) String position,

    @Schema(
        description = "Employee's department",
        example = "IT"
    ) String department,

    @Schema(
        description = "Indicates if the employee is active",
        example = "true"
    ) Boolean active
) {
}
