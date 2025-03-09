package rs.banka4.user_service.domain.loan.db;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;
import rs.banka4.user_service.domain.account.db.Account;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true)
    private Long loanNumber;
    private BigDecimal amount;
    private Integer repaymentPeriod;
    private LocalDate agreementDate;
    private LocalDate dueDate;
    private BigDecimal monthlyInstallment;
    private LocalDate nextInstallmentDate;
    private BigDecimal remainingDebt;
    private BigDecimal interestRate;

    @ManyToOne
    private Account account;

    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    @Enumerated(EnumType.STRING)
    private LoanType type;

    @Enumerated(EnumType.STRING)
    private InterestType interestType;

    public enum InterestType {
        FIXED, VARIABLE
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Loan loan = (Loan) o;
        return getId() != null && Objects.equals(getId(), loan.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Loan{" +
                "id=" + id +
                ", loanNumber=" + loanNumber +
                '}';
    }
}
