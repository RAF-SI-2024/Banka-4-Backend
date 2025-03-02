package rs.banka4.user_service.dto.requests;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import rs.banka4.user_service.models.Privilege;
import java.time.LocalDate;
import java.util.Set;

public record CreateEmployeeRequest(
        @NotBlank(message = "firstName is required")
        String firstName,

        @NotBlank(message = "lastName is required")
        String lastName,

        @NotBlank(message = "dateOfBirth is required")
        @JsonFormat(pattern = "MM-dd-yyyy")
        LocalDate dateOfBirth,

        @NotBlank(message = "email is required")
        @Email(message = "Invalid email!")
        String email,

        @NotBlank(message = "phone is required")
        String phone,

        @NotBlank(message = "address is required")
        String address,

        @NotBlank(message = "password is required")
        @Size(min = 8)
        String password,

        @NotBlank(message = "privilege is required")
        Set<Privilege> privilege,

        @NotBlank(message = "privilege is required")
        String position,

        @NotBlank(message = "department is required")
        String department
    ) {
}
