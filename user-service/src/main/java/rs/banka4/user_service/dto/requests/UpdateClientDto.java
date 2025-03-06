package rs.banka4.user_service.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "DTO for updating client details")
public record UpdateClientDto(
        @NotBlank(message = "First name is required and cannot be blank.")
        @Schema(description = "First name", example = "Mehmedalija")
        String firstName,

        @NotBlank(message = "Last name is required and cannot be blank.")
        @Schema(description = "Last name", example = "Doe")
        String lastName,

        @NotNull(message = "Date of birth is required and cannot be null.")
        @Schema(description = "Date of birth", example = "1990-01-01")
        LocalDate dateOfBirth,

        @NotBlank(message = "Gender is required and cannot be blank.")
        @Schema(description = "Gender", example = "Male")
        String gender,

        @Email(message = "Invalid email format.")
        @NotBlank(message = "Email address is required and cannot be blank.")
        @Schema(description = "Email address", example = "danny.jo@example.com")
        String email,

        @NotBlank(message = "Phone number is required and cannot be blank.")
        @Schema(description = "Phone number", example = "123-456-7890")
        String phone,

        @NotBlank(message = "Address is required and cannot be blank.")
        @Schema(description = "Address", example = "123 Main St")
        String address
) { }
