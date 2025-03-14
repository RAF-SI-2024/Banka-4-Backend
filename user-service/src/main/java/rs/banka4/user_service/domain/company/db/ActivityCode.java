package rs.banka4.user_service.domain.company.db;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

@Entity
@AllArgsConstructor
@Getter
@Setter
@RequiredArgsConstructor
@Builder
@Table(name = "activity_codes")
public class ActivityCode {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(
        nullable = false,
        unique = true
    )
    private String code;

    @Column(nullable = false)
    private String sector;

    @Column(nullable = false)
    private String branch;

    @Override
    public boolean equals(Object o) {
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
        ActivityCode that = (ActivityCode) o;
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

    @Override
    public String toString() {
        return "ActivityCode{" + "id=" + id + ", code='" + code + '\'' + '}';
    }
}
