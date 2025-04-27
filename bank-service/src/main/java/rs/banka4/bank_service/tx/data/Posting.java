package rs.banka4.bank_service.tx.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import lombok.With;
import rs.banka4.bank_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.rafeisen.common.currency.CurrencyCode;

@With
public record Posting(
    TxAccount account,
    BigDecimal amount,
    TxAsset asset
) {
    @JsonIgnore
    public MonetaryAmount amountAsMonetaryValue() {
        if (!(asset() instanceof TxAsset.Monas(MonetaryAsset(CurrencyCode cc)))) {
            throw new IllegalArgumentException("amountAsMonetaryValue can only be called on MONAS");
        }
        return new MonetaryAmount(amount(), cc);
    }
}
