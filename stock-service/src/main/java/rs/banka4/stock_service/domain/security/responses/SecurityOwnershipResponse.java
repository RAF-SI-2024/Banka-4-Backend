package rs.banka4.stock_service.domain.security.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SecurityOwnershipResponse(
    @Schema(
        description = "Type of security (e.g., Stock, Future, Forex)",
        example = "Stock"
    ) SecurityTypeDto type,

    @Schema(
        description = "Symbol of the security",
        example = "AAPL"
    ) String ticker,

    @Schema(
        description = "Quantity owned",
        example = "30"
    ) int amount,

    @Schema(
        description = "Current market price per unit",
        example = "172.50"
    ) BigDecimal price,

    @Schema(
        description = "Unrealized profit/loss",
        example = "495.00"
    ) BigDecimal profit,

    @Schema(
        description = "Timestamp of the last transaction",
        example = "2025-03-28T14:12:00+00:00"
    ) OffsetDateTime lastModified
) {
}
