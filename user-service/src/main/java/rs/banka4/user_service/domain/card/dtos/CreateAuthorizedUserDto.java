package rs.banka4.user_service.domain.card.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import rs.banka4.rafeisen.common.dto.Gender;

public record CreateAuthorizedUserDto(
    @Schema(
        description = "First name of the user",
        example = "Petar"
    ) String firstName,

    @Schema(
        description = "Last name of the user",
        example = "Petrović"
    ) String lastName,

    @Schema(
        description = "Date of birth",
        example = "1990-05-15"
    ) LocalDate dateOfBirth,

    @Schema(
        description = "User's gender (MALE or FEMALE)",
        example = "MALE"
    ) Gender gender,

    @Schema(
        description = "Email address of the user",
        example = "petar@example.com"
    ) String email,

    @Schema(
        description = "Phone number of the user",
        example = "+381611234567"
    ) String phoneNumber,

    @Schema(
        description = "Address of the user",
        example = "Njegoševa 25"
    ) String address
) {
}
