package rs.banka4.user_service.domain.card.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateAuthorizedCardDto(
        @Schema(description = "Account number", example = "1234567890123456")
        String accountNumber,

        @Schema(description = "Authorized user for business card")
        AuthorizedUserDto authorizedUserDto
) {
}
