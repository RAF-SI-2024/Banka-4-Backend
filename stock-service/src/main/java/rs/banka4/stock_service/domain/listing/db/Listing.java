package rs.banka4.stock_service.domain.listing.db;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;
import rs.banka4.stock_service.domain.exchanges.db.Exchange;
import rs.banka4.stock_service.domain.security.forex.db.ForexPair;
import rs.banka4.stock_service.domain.security.future.db.Future;
import rs.banka4.stock_service.domain.security.stock.db.Stock;

@Entity
@AllArgsConstructor
@Getter
@Setter
@RequiredArgsConstructor
@Builder
@Table(name = "listings")
public class Listing {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    private Stock stock;

    @ManyToOne
    private ForexPair forexPair;

    @ManyToOne
    private Future future;

    @Column(nullable = false)
    private String ticker;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    private Exchange exchange;

    @Column(nullable = false)
    private OffsetDateTime lastRefresh;

    @Column(nullable = false)
    private BigDecimal bid;

    @Column(nullable = false)
    private BigDecimal ask;

    @Builder.Default
    private boolean active = true;

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
        Listing listing = (Listing) o;
        return getId() != null && Objects.equals(getId(), listing.getId());
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
