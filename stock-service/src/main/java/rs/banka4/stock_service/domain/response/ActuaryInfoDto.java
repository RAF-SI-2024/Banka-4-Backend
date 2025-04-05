package rs.banka4.stock_service.domain.response;

import java.math.BigDecimal;
import rs.banka4.stock_service.domain.security.forex.db.CurrencyCode;

public record ActuaryInfoDto(
    boolean needsApproval,
    BigDecimal limitAmount,
    BigDecimal usedLimitAmount,
    CurrencyCode currencyCode
) {
}
