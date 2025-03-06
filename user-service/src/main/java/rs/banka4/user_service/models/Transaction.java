package rs.banka4.user_service.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

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

    @Column(nullable = false, unique = true)
    private String transactionNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "from_account_id", nullable = false)
    private Account fromAccount;

    @ManyToOne(optional = false)
    @JoinColumn(name = "to_account_id", nullable = false)
    private Account toAccount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "from_amount", nullable = false))
    })
    @AssociationOverrides({
            @AssociationOverride(name = "currency", joinColumns = @JoinColumn(name = "from_currency_id", nullable = false))
    })
    private MonetaryAmount from;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "to_amount", nullable = false))
    })
    @AssociationOverrides({
            @AssociationOverride(name = "currency", joinColumns = @JoinColumn(name = "to_currency_id", nullable = false))
    })
    private MonetaryAmount to;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "fee_amount"))
    })
    @AssociationOverrides({
            @AssociationOverride(name = "currency", joinColumns = @JoinColumn(name = "fee_currency_id"))
    })
    private MonetaryAmount fee;

    @Column(nullable = false, length = 255)
    private String recipient;

    @Column(length = 3)
    private String paymentCode;

    @Column(length = 50)
    private String referenceNumber;

    @Column(nullable = false, length = 500)
    private String paymentPurpose;

    @Column(nullable = false)
    private LocalDateTime paymentDateTime;

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