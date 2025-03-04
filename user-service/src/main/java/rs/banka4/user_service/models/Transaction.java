package rs.banka4.user_service.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Builder
@AllArgsConstructor
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(optional = false)
    private Account account;

    @Column(nullable = false)
    private LocalDateTime transactionDateTime;

    @Column(nullable = false, unique = true)
    private String orderReference;

    @ManyToOne(optional = false)
    private Client client;

    @Column(nullable = false, length = 500)
    private String transactionDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency.Code currency;

    @Column(nullable = false)
    private BigDecimal depositAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal withdrawalAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal reservedAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal reservedUsedAmount = BigDecimal.ZERO;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Transaction that = (Transaction) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
