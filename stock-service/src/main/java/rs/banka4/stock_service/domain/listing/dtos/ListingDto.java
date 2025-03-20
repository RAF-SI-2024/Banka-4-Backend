package rs.banka4.stock_service.domain.listing.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import rs.banka4.stock_service.domain.exchanges.dtos.ExchangeDto;
import rs.banka4.stock_service.domain.security.forex.dtos.ForexPairDto;
import rs.banka4.stock_service.domain.security.future.dtos.FutureDto;
import rs.banka4.stock_service.domain.security.stock.dtos.StockDto;

public record ListingDto(
    // TODO return SecurityDto instead of StockDto, FutureDto and ForexPairDto
    @Schema(
        description = "Listing ID",
        example = "123e4567-e89b-12d3-a456-426614174000"
    ) UUID id,

    @Schema(description = "Stock associated with the listing") StockDto stock,

    @Schema(description = "Forex pair associated with the listing") ForexPairDto forexPair,

    @Schema(description = "Future contract associated with the listing") FutureDto future,

    @Schema(
        description = "Ticker symbol of the listing",
        example = "AAPL"
    ) String ticker,

    @Schema(
        description = "Name of the listing",
        example = "Apple Inc."
    ) String name,

    @Schema(description = "Exchange where the listing is traded") ExchangeDto exchange,

    @Schema(
        description = "Last refresh time of the listing",
        example = "15:30"
    ) OffsetDateTime lastRefresh,

    @Schema(
        description = "Bid price of the listing",
        example = "150.50"
    ) BigDecimal bid,

    @Schema(
        description = "Ask price of the listing",
        example = "151.00"
    ) BigDecimal ask
) {
}
