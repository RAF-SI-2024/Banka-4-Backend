package rs.banka4.stock_service.domain.security.stock.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StockInfoDto(
    @JsonProperty("Name") String name,

    @JsonProperty("DividendYield") String dividendYield,

    @JsonProperty("SharesOutstanding") String outstandingShares,

    @JsonProperty("MarketCapitalization") String marketCap
) {
}
