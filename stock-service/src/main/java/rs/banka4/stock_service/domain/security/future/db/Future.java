package rs.banka4.stock_service.domain.security.future.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.OffsetDateTime;
import java.util.Objects;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.proxy.HibernateProxy;
import rs.banka4.stock_service.domain.security.Security;

@Entity(name = "futures")
@AllArgsConstructor
@Getter
@Setter
@RequiredArgsConstructor
@SuperBuilder
public class Future extends Security {

    @Column(nullable = false)
    private long contractSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UnitName contractUnit;

    @Column(nullable = false)
    private OffsetDateTime settlementDate;

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
        Future future = (Future) o;
        return getId() != null && Objects.equals(getId(), future.getId());
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
