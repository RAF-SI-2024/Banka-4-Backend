package rs.banka4.bank_service.tx.data;

import java.time.OffsetDateTime;
import rs.banka4.bank_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;

public record OptionDescription(
    ForeignBankId id,
    StockDescription stock,
    MonetaryAmount pricePerUnit,
    OffsetDateTime settlementDate,
    int amount,
    ForeignBankId negotiationId
) {
}
