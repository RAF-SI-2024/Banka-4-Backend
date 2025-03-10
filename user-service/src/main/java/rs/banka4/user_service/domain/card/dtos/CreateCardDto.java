package rs.banka4.user_service.domain.card.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreateCardDto(
        @Schema(description = "Account number", example = "1234567890123456")
        @NotBlank
        String accountNumber,

        @Schema(description = "Authorized user for business card")
        CreateAuthorizedUserDto createAuthorizedUserDto
) {
}
