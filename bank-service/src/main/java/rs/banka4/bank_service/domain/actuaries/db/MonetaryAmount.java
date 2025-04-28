package rs.banka4.bank_service.domain.actuaries.db;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import rs.banka4.rafeisen.common.currency.CurrencyCode;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@With
@Data
public class MonetaryAmount {
    @Column()
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "currency")
    private CurrencyCode currency;
}
