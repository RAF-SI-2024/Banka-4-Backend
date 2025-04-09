package rs.banka4.stock_service.domain.response;

import java.math.BigDecimal;
import rs.banka4.rafeisen.common.currency.CurrencyCode;

public record LimitPayload(
    BigDecimal limitAmount,
    CurrencyCode limitCurrencyCode
) {
}
