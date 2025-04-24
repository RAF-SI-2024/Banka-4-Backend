package rs.banka4.bank_service.tx.data;

import java.time.OffsetDateTime;
import rs.banka4.bank_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;

public record OtcOffer(
    StockDescription stock,
    OffsetDateTime settlementDate,
    MonetaryAmount pricePerUnit,
    MonetaryAmount premium,
    ForeignBankId buyerId,
    ForeignBankId sellerId,
    int amount,
    ForeignBankId lastModifiedBy
) {
}
