package rs.banka4.user_service.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "DTO for creating Client")
public record CreateClientDto(
        @Schema(description = "Client's first name", example = "Ognjen")
        @NotBlank(message = "First name is required")
        String firstName,

        @Schema(description = "Client's last name", example = "Jukic")
        @NotBlank(message = "Last name is required")
        String lastName,

        @Schema(description = "Client's username", example = "funfa2c1t")
        @NotBlank(message = "Username is required")
        String username,

        @Schema(description = "Client's date of birth", example = "1990-05-15")
        @NotNull(message = "Date of birth is required")
        LocalDate dateOfBirth,

        @Schema(description = "Client's gender", example = "man")
        @NotBlank(message = "Gender is required")
        String gender,

        @Schema(description = "Client's email address", example = "mljubic9422112rn@raf.rs")
        @Email(message = "Email should be valid")
        @NotBlank(message = "Email is required")
        String email,

        @Schema(description = "Client's phone number", example = "+1234567890")
        @NotBlank(message = "Phone is required")
        String phone,

        @Schema(description = "Client's address", example = "123 Grove Street, City, Country")
        @NotBlank(message = "Address is required")
        String address,

        @Schema(description = "Client's privileges", example = "[\"TRADE_STOCKS\", \"CONTRACTS\"]")
        @NotNull(message = "Privileges are required")
        List<String> privilege,

        @Schema(description = "Client's accounts", example = "[{}]")
        @Valid
        List<CreateAccountDto> accounts
) {
}
