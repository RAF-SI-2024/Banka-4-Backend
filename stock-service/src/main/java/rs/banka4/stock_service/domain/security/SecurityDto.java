package rs.banka4.stock_service.domain.security;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;
import rs.banka4.stock_service.domain.security.forex.dtos.ForexPairDto;
import rs.banka4.stock_service.domain.security.future.dtos.FutureDto;
import rs.banka4.stock_service.domain.security.stock.dtos.StockDto;

public record SecurityDto(
    @Schema(
        description = "Security ID",
        example = "123e4567-e89b-12d3-a456-426614174000"
    ) UUID id,

    @Schema(
        description = "Security name",
        nullable = true
    ) String name,

    @Schema(
        description = "Security price, derived info",
        nullable = true
    ) BigDecimal price,

    @Schema(
        description = "Stock details",
        nullable = true
    ) StockDto stock,

    @Schema(
        description = "Future contract details",
        nullable = true
    ) FutureDto future,

    @Schema(
        description = "Forex pair details",
        nullable = true
    ) ForexPairDto forexPair
) {
}
