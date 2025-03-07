package rs.banka4.user_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import rs.banka4.user_service.models.Privilege;
import rs.banka4.user_service.models.User;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record ClientDto(
        @Schema(description = "Client ID", example = "1fad2c01-f82f-41a6-822c-8ca1b3232575")
        UUID id,
        @Schema(description = "First name", example = "Mehmedalija")
        String firstName,
        @Schema(description = "Last name", example = "Doe")
        String lastName,
        @Schema(description = "Date of birth", example = "1990-01-01")
        LocalDate dateOfBirth,
        @Schema(description = "Gender", example = "Male")
        User.Gender gender,
        @Schema(description = "Email address", example = "danny.jo@example.com")
        String email,
        @Schema(description = "Phone number", example = "123-456-7890")
        String phone,
        @Schema(description = "Address", example = "123 Main St")
        String address,
        @Schema(description = "List of privileges", example = "[\"CAN_TRADE\"]")
        EnumSet<Privilege> privileges
) {
}
