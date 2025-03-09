package rs.banka4.user_service.domain.card.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import rs.banka4.user_service.domain.card.db.CardName;
import rs.banka4.user_service.domain.card.db.CardStatus;
import rs.banka4.user_service.domain.card.db.CardType;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "DTO for card details")
public record CardDto(

        @Schema(description = "Card ID", example = "11111111-2222-3333-4444-555555555555")
        UUID id,

        @Schema(description = "Card number", example = "1234567890123456")
        String cardNumber,

        @Schema(description = "Card CVV", example = "123")
        String cvv,

        @Schema(description = "Cardholder name", example = "Visa")
        CardName cardName,

        @Schema(description = "Type of card", example = "Debit")
        CardType cardType,

        @Schema(description = "Credit limit for the card", example = "5000.00")
        BigDecimal limit,

        @Schema(description = "Current status of the card", example = "ACTIVE")
        CardStatus cardStatus,

        @Schema(description = "Authorized user for business card")
        AuthorizedUserDto authorizedUserDto

) { }

