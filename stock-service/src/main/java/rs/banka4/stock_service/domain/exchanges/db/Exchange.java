package rs.banka4.stock_service.domain.exchanges.db;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;
import rs.banka4.rafeisen.common.currency.CurrencyCode;

@Entity
@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
@Table(name = "exchanges")
public class Exchange {
    // https://drive.google.com/file/d/1H8FPNSkKhvkHjkJtMSJxwsnMMt7VIQjG/view za seed
    @Id
    @Builder.Default
    public UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private String exchangeName;

    @Column(nullable = false)
    private String exchangeAcronym;

    @Column(nullable = false)
    private String exchangeMICCode;

    @Column(nullable = false)
    private String polity;

    @Enumerated(EnumType.STRING)
    @Column(
        nullable = false,
        columnDefinition = "currency"
    )
    private CurrencyCode currency;

    @Column(nullable = false)
    private String timeZone; // (e.g., "America/New_York")

    @Column(nullable = false)
    private OffsetDateTime openTime;

    @Column(nullable = false)
    private OffsetDateTime closeTime;

    @Builder.Default
    @Column(nullable = false)
    private LocalDate createdAt = LocalDate.now();

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
        Exchange exchange = (Exchange) o;
        return getId() != null && Objects.equals(getId(), exchange.getId());
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
