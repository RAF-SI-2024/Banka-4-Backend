package rs.banka4.stock_service.domain.security.stock.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record StockDto(
    @Schema(
        description = "Stock ID",
        example = "123e4567-e89b-12d3-a456-426614174000"
    ) UUID id,
    @Schema(
        description = "Human readable name",
        example = "Microsoft Company",
        nullable = true
    ) String name,
    @Schema(
        description = "Number of outstanding shares",
        example = "1000000"
    ) long outstandingShares,

    @Schema(
        description = "Dividend yield of the stock",
        example = "2.5"
    ) BigDecimal dividendYield,

    @Schema(
        description = "Stock creation date",
        example = "2024-03-19"
    ) OffsetDateTime createdAt,

    @Schema(
        description = "Current price of the stock",
        example = "150.75",
        nullable = true
    ) BigDecimal price,
    @Schema(
        description = "Market capitalization of the stock",
        example = "150750000.00",
        nullable = true
    ) BigDecimal marketCap
) {
}
