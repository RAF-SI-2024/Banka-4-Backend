package rs.banka4.stock_service.domain.security.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

public record TotalProfitResponse(
    @Schema(
        description = "Total unrealized profit across all stocks",
        example = "1095.00"
    ) BigDecimal totalProfit,

    @Schema(
        description = "Currency code for profit calculations",
        example = "USD"
    ) String currency
) {
}
