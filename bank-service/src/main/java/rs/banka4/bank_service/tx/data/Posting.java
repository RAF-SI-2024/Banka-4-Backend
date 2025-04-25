package rs.banka4.bank_service.tx.data;

import java.math.BigDecimal;
import lombok.With;

@With
public record Posting(
    TxAccount account,
    BigDecimal amount,
    TxAsset asset
) {
}
