package rs.banka4.user_service.domain.account.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import rs.banka4.user_service.domain.user.Gender;
import rs.banka4.user_service.domain.user.Privilege;

@Schema(description = "Dto for creating or choosing exist Client")
public record AccountClientIdDto(
    @Schema(
        description = "Client ID",
        example = "1fad2c01-f82f-41a6-822c-8ca1b3232575"
    ) @Nullable UUID id,

    @Schema(
        description = "Client's first name",
        example = "Ognjen"
    ) @NotBlank(message = "First name is required") String firstName,

    @Schema(
        description = "Client's last name",
        example = "Jukic"
    ) @NotBlank(message = "Last name is required") String lastName,

    @Schema(
        description = "Client's date of birth",
        example = "1990-05-15"
    ) @NotNull(message = "Date of birth is required") LocalDate dateOfBirth,

    @Schema(
        description = "Client's gender (MALE or FEMALE)",
        example = "MALE"
    ) @NotBlank(message = "Gender is required") Gender gender,

    @Schema(
        description = "Client's email address",
        example = "mljubic9422112rn@raf.rs"
    )
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required") String email,

    @Schema(
        description = "Client's phoneNumber number",
        example = "+381658974125"
    ) @NotBlank(message = "Phone is required") String phone,

    @Schema(
        description = "Client's address",
        example = "123 Grove Street, City, Country"
    ) @NotBlank(message = "Address is required") String address,

    @Schema(
        description = "Client's privileges",
        example = "[\"TRADE_STOCKS\", \"CONTRACTS\"]"
    ) @NotNull(message = "Privileges are required") Set<Privilege> privilege
) {
}
