package rs.banka4.user_service.domain.card.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import rs.banka4.user_service.domain.user.Gender;

import java.time.LocalDate;

public record CreateAuthorizedUserDto(
        @Schema(description = "First name of the user", example = "Petar")
        String firstName,

        @Schema(description = "Last name of the user", example = "Petrović")
        String lastName,

        @Schema(description = "Date of birth", example = "1990-05-15")
        LocalDate dateOfBirth,

        @Schema(description = "Client's gender (Male or Female)", example = "Male")
        @Pattern(regexp = "Male|Female", message = "Gender must be Male or Female")
        @NotBlank(message = "Gender is required")
        String gender,

        @Schema(description = "Email address of the user", example = "petar@example.com")
        String email,

        @Schema(description = "Phone number of the user", example = "+381645555555")
        String phoneNumber,

        @Schema(description = "Address of the user", example = "Njegoševa 25")
        String address
) {
}
