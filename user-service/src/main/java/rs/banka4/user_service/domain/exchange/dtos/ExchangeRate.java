package rs.banka4.user_service.domain.exchange.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record ExchangeRate(
    @JsonProperty("Base") String base,
    @JsonProperty("Quote") String quote,
    @JsonProperty("Buy") BigDecimal buy,
    @JsonProperty("Neutral") BigDecimal neutral,
    @JsonProperty("Sell") BigDecimal sell
) {
}
