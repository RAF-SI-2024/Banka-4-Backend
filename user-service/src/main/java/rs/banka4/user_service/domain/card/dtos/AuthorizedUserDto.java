package rs.banka4.user_service.domain.card.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import rs.banka4.user_service.domain.user.Gender;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "DTO representing an authorized user")
public record AuthorizedUserDto(

        @Schema(description = "User ID", example = "11111111-2222-3333-4444-555555555555")
        UUID id,

        @Schema(description = "First name of the user", example = "Petar")
        String firstName,

        @Schema(description = "Last name of the user", example = "Petrović")
        String lastName,

        @Schema(description = "Date of birth", example = "1990-05-15")
        LocalDate dateOfBirth,

        @Schema(description = "Gender of the user", example = "M")
        Gender gender,

        @Schema(description = "Email address of the user", example = "petar@example.com")
        String email,

        @Schema(description = "Phone number of the user", example = "+381645555555")
        String phoneNumber,

        @Schema(description = "Address of the user", example = "Njegoševa 25")
        String address,

        @Schema(description = "Date when the card was created", example = "2023-03-09")
        LocalDate createdAt,

        @Schema(description = "Expiration date of the card", example = "2024-03-09")
        LocalDate expiresAt
) {
}

