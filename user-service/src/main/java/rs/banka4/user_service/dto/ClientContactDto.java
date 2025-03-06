package rs.banka4.user_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ClientContactDto(
        @Schema(description = "First name", example = "Mehmedalija")
        String firstName,
        @Schema(description = "Last name", example = "Doe")
        String lastName,
        @Schema(description = "Account number", example = "444000000000123456")
        String accountNumber
) {
}
