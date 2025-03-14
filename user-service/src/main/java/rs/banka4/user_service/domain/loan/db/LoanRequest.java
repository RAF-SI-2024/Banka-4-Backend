package rs.banka4.user_service.domain.loan.db;

import jakarta.persistence.*;
import lombok.*;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.currency.db.Currency;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "loan_requests")
public class LoanRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "currency_id")
    private Currency currency;

    private String employmentStatus;

    private Integer employmentPeriod;

    private Integer repaymentPeriod;

    private String purposeOfLoan;

    @ManyToOne
    private Account account;

    @OneToOne
    private Loan loan;

    private BigDecimal monthlyIncome;

    @Enumerated(EnumType.STRING)
    private LoanType type;

    @Enumerated(EnumType.STRING)
    private Loan.InterestType interestType;

    private String contactPhone;
}
