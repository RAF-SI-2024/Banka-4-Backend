package rs.banka4.user_service.domain.card.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import rs.banka4.user_service.domain.card.db.CardName;
import rs.banka4.user_service.domain.card.db.CardStatus;
import rs.banka4.user_service.domain.card.db.CardType;
import rs.banka4.user_service.domain.user.client.dtos.ClientDto;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "DTO for card details")
public record CardDto(

        @Schema(description = "Card number", example = "1234567890123456")
        String cardNumber,

        @Schema(description = "Card CVV", example = "123")
        String cvv,

        @Schema(description = "Cardholder name", example = "Visa")
        CardName cardName,

        @Schema(description = "Date of creation", example = "1990-05-15")
        LocalDate creationDate,

        @Schema(description = "Date of expiration", example = "1994-05-15")
        LocalDate expirationDate,

        @Schema(description = "Type of card", example = "Debit")
        CardType cardType,

        @Schema(description = "Credit limit for the card", example = "5000.00")
        BigDecimal limit,

        @Schema(description = "Current status of the card", example = "ACTIVE")
        CardStatus cardStatus,

        @Schema(description = "Account number", example = "215351385938112")
        String accountNumber,

        @Schema(description = "Credit card owner")
        ClientDto client,

        @Schema(description = "Authorized user for business card")
        AuthorizedUserDto authorizedUserDto

) { }

