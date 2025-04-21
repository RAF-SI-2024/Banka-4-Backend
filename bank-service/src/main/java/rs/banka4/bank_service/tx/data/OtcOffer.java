package rs.banka4.bank_service.tx.data;

import java.time.OffsetDateTime;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;
import rs.banka4.bank_service.domain.transaction.db.MonetaryAmount;

public record OtcOffer(
    StockDescription stock,
    OffsetDateTime settlementDate,
    MonetaryAmount pricePerUnit,
    ForeignBankId buyerId,
    ForeignBankId sellerId,
    int amount,
    ForeignBankId lastModifiedBy
) {
}
