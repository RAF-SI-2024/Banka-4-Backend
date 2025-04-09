package rs.banka4.stock_service.domain.security.forex.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import rs.banka4.rafeisen.common.currency.CurrencyCode;
import rs.banka4.stock_service.domain.security.forex.db.ForexLiquidity;

public record RealTimeCurrencyExchangeRate(
    @JsonProperty("1. From_Currency Code")
    @Schema(
        description = "Base currency of the forex pair",
        example = "USD"
    ) CurrencyCode baseCurrency,

    @JsonProperty("3. To_Currency Code")
    @Schema(
        description = "Quote currency of the forex pair",
        example = "EUR"
    ) CurrencyCode quoteCurrency,

    @Schema(
        description = "Liquidity level of the forex pair",
        example = "HIGH"
    ) ForexLiquidity liquidity,

    @JsonProperty("5. Exchange Rate")
    @Schema(
        description = "Exchange rate of the forex pair",
        example = "146.0"
    ) BigDecimal exchangeRate
) {

}
