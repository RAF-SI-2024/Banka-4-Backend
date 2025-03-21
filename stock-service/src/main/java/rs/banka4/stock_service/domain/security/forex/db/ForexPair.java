package rs.banka4.stock_service.domain.security.forex.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.Objects;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.proxy.HibernateProxy;
import rs.banka4.stock_service.domain.security.Security;

@Entity(name = "forex_pairs")
@AllArgsConstructor
@Getter
@Setter
@RequiredArgsConstructor
@SuperBuilder
public class ForexPair extends Security {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurrencyCode baseCurrency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurrencyCode quoteCurrency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ForexLiquidity liquidity;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass =
            o instanceof HibernateProxy
                ? ((HibernateProxy) o).getHibernateLazyInitializer()
                    .getPersistentClass()
                : o.getClass();
        Class<?> thisEffectiveClass =
            this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer()
                    .getPersistentClass()
                : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        ForexPair forexPair = (ForexPair) o;
        return getId() != null && Objects.equals(getId(), forexPair.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer()
                .getPersistentClass()
                .hashCode()
            : getClass().hashCode();
    }
}
