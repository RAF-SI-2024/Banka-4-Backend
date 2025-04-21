package rs.banka4.bank_service.tx.data;

import java.time.OffsetDateTime;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;
import rs.banka4.bank_service.domain.transaction.db.MonetaryAmount;

public record OptionDescription(
    ForeignBankId id,
    StockDescription stock,
    MonetaryAmount pricePerUnit,
    OffsetDateTime settlementDate,
    int amount
) {
}
