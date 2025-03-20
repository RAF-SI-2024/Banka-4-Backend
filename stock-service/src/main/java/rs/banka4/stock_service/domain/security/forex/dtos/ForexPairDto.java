package rs.banka4.stock_service.domain.security.forex.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;
import rs.banka4.stock_service.domain.security.forex.db.CurrencyCode;
import rs.banka4.stock_service.domain.security.forex.db.ForexLiquidity;

public record ForexPairDto(
    @Schema(
        description = "Forex pair ID",
        example = "123e4567-e89b-12d3-a456-426614174000"
    ) UUID id,

    @Schema(
        description = "Base currency of the forex pair",
        example = "USD"
    ) CurrencyCode baseCurrency,

    @Schema(
        description = "Quote currency of the forex pair",
        example = "EUR"
    ) CurrencyCode quoteCurrency,

    @Schema(
        description = "Liquidity level of the forex pair",
        example = "HIGH"
    ) ForexLiquidity liquidity,

    @Schema(
        description = "Current price of the forex pair",
        example = "1.2345",
        nullable = true
    ) BigDecimal price
) {
}
