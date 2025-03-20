package rs.banka4.user_service.domain.user.client.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.UUID;
import rs.banka4.user_service.domain.user.Gender;
import rs.banka4.user_service.domain.user.Privilege;

@Schema(description = "DTO for returned clients in response")
public record ClientDto(
    @Schema(
        description = "Client ID",
        example = "1fad2c01-f82f-41a6-822c-8ca1b3232575"
    ) UUID id,

    @Schema(
        description = "First name",
        example = "Mehmedalija"
    ) String firstName,

    @Schema(
        description = "Last name",
        example = "Doe"
    ) String lastName,
    @Schema(
        description = "Date of birth",
        example = "1990-01-01"
    ) LocalDate dateOfBirth,

    @Schema(
        description = "Gender (MALE or FEMALE)",
        example = "MALE"
    ) Gender gender,

    @Schema(
        description = "Email address",
        example = "danny.jo@example.com"
    ) String email,

    @Schema(
        description = "Phone number",
        example = "+381684523697"
    ) String phone,

    @Schema(
        description = "Address",
        example = "123 Main St"
    ) String address,

    @Schema(
        description = "List of privileges",
        example = "[\"CAN_TRADE\"]"
    ) EnumSet<Privilege> privileges,

    @Schema(
        description = "Flag that indicates if the user has completed 2FA setup",
        example = "False"
    ) Boolean has2FA
) {
}
