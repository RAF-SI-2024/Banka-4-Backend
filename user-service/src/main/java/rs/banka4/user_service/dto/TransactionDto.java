package rs.banka4.user_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import rs.banka4.user_service.models.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "DTO for transactions")
public record TransactionDto(

        @Schema(description = "Transaction ID")
        UUID id,

        @Schema(description = "Account ID associated with the transaction")
        UUID accountId,

        @Schema(description = "Transaction date and time")
        LocalDateTime transactionDateTime,

        @Schema(description = "Reference to the order")
        String orderReference,

        @Schema(description = "Client ID who performed the transaction")
        UUID clientId,

        @Schema(description = "Description of the transaction")
        String transactionDescription,

        @Schema(description = "Transaction currency")
        Currency.Code currency,

        @Schema(description = "Deposit amount")
        BigDecimal depositAmount,

        @Schema(description = "Withdrawal amount")
        BigDecimal withdrawalAmount,

        @Schema(description = "Reserved amount for securities purchase")
        BigDecimal reservedAmount,

        @Schema(description = "Reserved funds used for the transaction")
        BigDecimal reservedUsedAmount
) {
}
