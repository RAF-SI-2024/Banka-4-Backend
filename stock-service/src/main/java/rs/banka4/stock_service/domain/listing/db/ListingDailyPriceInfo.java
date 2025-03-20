package rs.banka4.stock_service.domain.listing.db;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;
import rs.banka4.stock_service.domain.exchanges.db.Exchange;

@Entity
@AllArgsConstructor
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Builder
@Table(name = "listing_daily_price_info")
public class ListingDailyPriceInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String ticker;

    @Column(nullable = false)
    private String name;

    @ManyToOne(
        optional = false,
        fetch = FetchType.LAZY
    )
    @JoinColumn(
        name = "exchange_id",
        nullable = false
    )
    @ToString.Exclude
    private Exchange exchange;

    @Column(nullable = false)
    private OffsetDateTime date;

    @Column(nullable = false)
    private BigDecimal lastPrice;

    @Column(nullable = false)
    private BigDecimal askHigh; // highest fulfilled ask

    @Column(nullable = false)
    private BigDecimal bigLow; // lowest fulfilled bid

    @Column(nullable = false)
    private BigDecimal change;

    @Column(nullable = false)
    private Integer volume;

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
        ListingDailyPriceInfo that = (ListingDailyPriceInfo) o;
        return getId() != null && Objects.equals(getId(), that.getId());
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
