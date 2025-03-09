package rs.banka4.user_service.domain.card.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import rs.banka4.user_service.domain.card.db.CardName;
import rs.banka4.user_service.domain.card.db.CardType;

public record CreateCardDto(
        @Schema(description = "Account number", example = "1234567890123456")
        String accountNumber,

        @Schema(description = "Cardholder name", example = "Visa")
        CardName cardName,

        @Schema(description = "Type of card", example = "Debit")
        CardType cardType
) {
}
