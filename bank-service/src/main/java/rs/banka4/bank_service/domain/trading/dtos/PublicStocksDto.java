package rs.banka4.bank_service.domain.trading.dtos;

import java.time.OffsetDateTime;
import java.util.UUID;
import rs.banka4.bank_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.bank_service.domain.listing.dtos.SecurityType;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;

public record PublicStocksDto(
    SecurityType securityType,
    ForeignBankId sellerId,
    UUID stockId,
    String ownerUsername,
    String ticker,
    String name,
    int amount,
    MonetaryAmount activePrice,
    OffsetDateTime lastUpdated
) {
}
