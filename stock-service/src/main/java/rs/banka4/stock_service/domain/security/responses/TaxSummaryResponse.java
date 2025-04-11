package rs.banka4.stock_service.domain.security.responses;

import java.math.BigDecimal;

public record TaxSummaryResponse(
    BigDecimal paidTaxThisYear,
    BigDecimal unpaidTaxThisMonth,
    String currency
) {
}
