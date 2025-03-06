package rs.banka4.user_service.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Builder
@AllArgsConstructor
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // todo: check
    private UUID id;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    // Balance that is available for withdrawal
    @Builder.Default
    private BigDecimal availableBalance = BigDecimal.ZERO;

    private BigDecimal accountMaintenance;

    @Builder.Default
    private LocalDate createdDate = LocalDate.now();

    @Builder.Default
    private LocalDate expirationDate = LocalDate.now().plusYears(5);

    // When client is deleted, it must be set to false
    @Column(nullable = false)
    private boolean active;

    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Builder.Default
    private BigDecimal dailyLimit = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal monthlyLimit = BigDecimal.ZERO;

    @ManyToOne
    private Employee employee;

    @ManyToOne
    private Client client;

    @ManyToOne
    private Company company;

    @ManyToOne
    private Currency currency;

    @PrePersist
    public void setAccountMaintenance() {
        if (this.currency != null && Currency.Code.RSD.equals(this.getCurrency().getCode())) {
            setAccountMaintenance(new BigDecimal("100.00"));
        } else {
            setAccountMaintenance(new BigDecimal("0"));
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Account account = (Account) o;
        return getId() != null && Objects.equals(getId(), account.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
