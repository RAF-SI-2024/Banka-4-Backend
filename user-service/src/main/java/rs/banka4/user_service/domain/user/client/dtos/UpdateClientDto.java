package rs.banka4.user_service.domain.user.client.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

@Schema(description = "DTO for updating client details")
public record UpdateClientDto(

        @Schema(description = "First name", example = "Mehmedalija")
        String firstName,

        @Schema(description = "Last name", example = "Doe")
        String lastName,

        @Schema(description = "Date of birth", example = "1990-01-01")
        LocalDate dateOfBirth,

        @Pattern(regexp = "Male|Female", message = "Gender must be Male or Female")
        @Schema(description = "Gender", example = "Male")
        String gender,

        @Email(message = "Invalid email format.")
        @Schema(description = "Email address", example = "danny.jo@example.com")
        String email,

        @Schema(description = "Phone number", example = "123-456-7890")
        String phone,

        @Schema(description = "Address", example = "123 Main St")
        String address
) { }
