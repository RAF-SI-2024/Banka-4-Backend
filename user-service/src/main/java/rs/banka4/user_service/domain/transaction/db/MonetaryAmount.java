package rs.banka4.user_service.domain.transaction.db;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.banka4.user_service.domain.currency.db.Currency;

import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MonetaryAmount {

    @Column(nullable = false)
    private BigDecimal amount;

    @ManyToOne(optional = false)
    @JoinColumn(name = "currency_id", nullable = false, insertable = false, updatable = false)
    private Currency currency;
}