package rs.banka4.user_service.domain.user.client.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

public record ClientContactDto(
        @Schema(description = "Client Account nickname", example = "Wasketov racun od firme")
        String nickname,
        @Schema(description = "Account number", example = "444000000000123456")
        String accountNumber
) {
}
