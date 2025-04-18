package rs.banka4.bank_service.integration.seeder;

import static rs.banka4.bank_service.runners.TestDataRunner.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rs.banka4.bank_service.domain.account.db.Account;
import rs.banka4.bank_service.domain.card.db.Card;
import rs.banka4.bank_service.domain.card.db.CardName;
import rs.banka4.bank_service.domain.card.db.CardStatus;
import rs.banka4.bank_service.domain.card.db.CardType;
import rs.banka4.bank_service.domain.loan.db.InterestRate;
import rs.banka4.bank_service.domain.loan.db.Loan;
import rs.banka4.bank_service.domain.loan.db.LoanStatus;
import rs.banka4.bank_service.domain.loan.db.LoanType;
import rs.banka4.bank_service.repositories.*;
import rs.banka4.rafeisen.common.currency.CurrencyCode;

@Component
public class TestDataSeeder {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private LoanInstallmentRepository loanInstallmentRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private InterestRateRepository interestRateRepository;


    /**
     * Kreira i čuva valutu u bazi.
     *
     * @return Sačuvani Currency objekat.
     */
    public CurrencyCode seedCurrency() {
        return CurrencyCode.EUR;
    }

    /**
     * Kreira i čuva nalog u bazi sa podrazumevanom valutom.
     *
     * @return Sačuvani Account objekat.
     */
    public Account seedAccount() {
        var currency = seedCurrency();

        return accountRepository.saveAndFlush(
            Account.builder()
                .accountNumber("123456789")
                .balance(BigDecimal.valueOf(10000))
                .availableBalance(BigDecimal.valueOf(8000))
                .active(true)
                .currency(currency)
                .build()
        );
    }

    private InterestRate createInterestRate(
        UUID interestId,
        long minAmount,
        Long maxAmount,
        double fixedRate
    ) {
        return InterestRate.builder()
            .id(interestId)
            .minAmount(BigDecimal.valueOf(minAmount))
            .maxAmount(maxAmount != null ? BigDecimal.valueOf(maxAmount) : null)
            .fixedRate(BigDecimal.valueOf(fixedRate))
            .dateActiveFrom(LocalDate.now())
            .dateActiveTo(
                LocalDate.now()
                    .plusYears(1)
            )
            .build();
    }

    private void interestRateSeeder() {
        List<InterestRate> interestRates =
            List.of(
                createInterestRate(LOAN_INTEREST_0_500000, 0, 500000L, 6.25),
                createInterestRate(LOAN_INTEREST_500001_1000000, 500001, 1000000L, 6.00),
                createInterestRate(LOAN_INTEREST_1000001_2000000, 1000001, 2000000L, 5.75),
                createInterestRate(LOAN_INTEREST_2000001_5000000, 2000001, 5000000L, 5.50),
                createInterestRate(LOAN_INTEREST_5000001_10000000, 5000001, 10000000L, 5.25),
                createInterestRate(LOAN_INTEREST_10000001_20000000, 10000001, 20000000L, 5.00),
                createInterestRate(LOAN_INTEREST_20000001_2000000100, 20000001, 2000000100L, 4.75) // No
                // upper limit
            );

        interestRateRepository.saveAllAndFlush(interestRates);
    }

    /**
     * Kreira i čuva kredit u bazi.
     *
     * @return Sačuvani Loan objekat.
     */
    public Loan seedLoan(Account account) {
        interestRateSeeder();
        return loanRepository.saveAndFlush(
            Loan.builder()
                .loanNumber(100001L)
                .amount(BigDecimal.valueOf(5000))
                .repaymentPeriod(36)
                .agreementDate(LocalDate.now())
                .dueDate(
                    LocalDate.now()
                        .plusYears(3)
                )
                .monthlyInstallment(BigDecimal.valueOf(150))
                .nextInstallmentDate(
                    LocalDate.now()
                        .plusMonths(1)
                )
                .remainingDebt(BigDecimal.valueOf(4500))
                .baseInterestRate(BigDecimal.valueOf(3.5))
                .account(account)
                .status(LoanStatus.PROCESSING)
                .type(LoanType.CASH)
                .interestType(Loan.InterestType.FIXED)
                .interestRate(
                    interestRateRepository.findById(LOAN_INTEREST_0_500000)
                        .get()
                )
                .build()
        );
    }

    public Loan seedRejectedLoan(Account account) {
        interestRateSeeder();
        return loanRepository.saveAndFlush(
            Loan.builder()
                .loanNumber(100002L)
                .amount(BigDecimal.valueOf(5000))
                .repaymentPeriod(36)
                .agreementDate(LocalDate.now())
                .dueDate(
                    LocalDate.now()
                        .plusYears(3)
                )
                .monthlyInstallment(BigDecimal.valueOf(150))
                .nextInstallmentDate(
                    LocalDate.now()
                        .plusMonths(1)
                )
                .remainingDebt(BigDecimal.valueOf(4500))
                .baseInterestRate(BigDecimal.valueOf(3.5))
                .account(account)
                .status(LoanStatus.REJECTED)
                .type(LoanType.CASH)
                .interestType(Loan.InterestType.FIXED)
                .interestRate(
                    interestRateRepository.findById(LOAN_INTEREST_0_500000)
                        .get()
                )
                .build()
        );
    }

    /**
     * Kreira i čuva 2 kredita u bazi.
     *
     * @return Listu sačuvanih Loans objekata.
     */
    public List<Loan> seedLoans(Account account) {
        interestRateSeeder();
        Loan loan1 =
            loanRepository.saveAndFlush(
                Loan.builder()
                    .loanNumber(100001L)
                    .amount(BigDecimal.valueOf(5000))
                    .repaymentPeriod(36)
                    .agreementDate(LocalDate.now())
                    .dueDate(
                        LocalDate.now()
                            .plusYears(3)
                    )
                    .monthlyInstallment(BigDecimal.valueOf(150))
                    .nextInstallmentDate(
                        LocalDate.now()
                            .plusMonths(1)
                    )
                    .remainingDebt(BigDecimal.valueOf(4500))
                    .baseInterestRate(BigDecimal.valueOf(3.5))
                    .account(account)
                    .status(LoanStatus.APPROVED)
                    .type(LoanType.CASH)
                    .interestType(Loan.InterestType.FIXED)
                    .interestRate(
                        interestRateRepository.findById(LOAN_INTEREST_0_500000)
                            .get()
                    )
                    .build()
            );

        Loan loan2 =
            loanRepository.saveAndFlush(
                Loan.builder()
                    .loanNumber(100002L)
                    .amount(BigDecimal.valueOf(10000))
                    .repaymentPeriod(48)
                    .agreementDate(
                        LocalDate.now()
                            .minusMonths(3)
                    )
                    .dueDate(
                        LocalDate.now()
                            .plusYears(4)
                    )
                    .monthlyInstallment(BigDecimal.valueOf(250))
                    .nextInstallmentDate(
                        LocalDate.now()
                            .plusMonths(1)
                    )
                    .remainingDebt(BigDecimal.valueOf(8000))
                    .baseInterestRate(BigDecimal.valueOf(4.0))
                    .account(account)
                    .status(LoanStatus.PAID_OFF)
                    .type(LoanType.CASH)
                    .interestType(Loan.InterestType.VARIABLE)
                    .interestRate(
                        interestRateRepository.findById(LOAN_INTEREST_0_500000)
                            .get()
                    )
                    .build()
            );

        return List.of(loan1, loan2);
    }

    /**
     * Kreira i čuva karticu u bazi.
     *
     * @return Karticu sačuvan Card objekat.
     */
    public Card seedActiveCard(Account account) {
        return cardRepository.saveAndFlush(
            Card.builder()
                .cardNumber("1234567810345678")
                .cvv("123")
                .cardName(CardName.VISA)
                .cardType(CardType.DEBIT)
                .account(account)
                .cardStatus(CardStatus.ACTIVATED)
                .limit(BigDecimal.valueOf(10000))
                .createdAt(LocalDate.now())
                .expiresAt(
                    LocalDate.now()
                        .plusYears(5)
                )
                .build()
        );
    }

    public Card seedDeactivatedCard(Account account) {
        return cardRepository.saveAndFlush(
            Card.builder()
                .cardNumber("1234000810345678")
                .cvv("123")
                .cardName(CardName.VISA)
                .cardType(CardType.DEBIT)
                .account(account)
                .cardStatus(CardStatus.DEACTIVATED)
                .limit(BigDecimal.valueOf(10000))
                .createdAt(LocalDate.now())
                .expiresAt(
                    LocalDate.now()
                        .plusYears(5)
                )
                .build()
        );
    }

    public Card seedBlockedCard(Account account) {
        return cardRepository.saveAndFlush(
            Card.builder()
                .cardNumber("1234567810300078")
                .cvv("123")
                .cardName(CardName.VISA)
                .cardType(CardType.DEBIT)
                .account(account)
                .cardStatus(CardStatus.BLOCKED)
                .limit(BigDecimal.valueOf(10000))
                .createdAt(LocalDate.now())
                .expiresAt(
                    LocalDate.now()
                        .plusYears(5)
                )
                .build()
        );
    }

    /**
     * Creates and saves two accounts in the database.
     *
     * @return List of two saved Account objects.
     */
    public List<Account> seedTwoAccounts() {
        var currency = seedCurrency();
        Account account1 = seedAccount();
        Account account2 =
            Account.builder()
                .accountNumber("0987654321")
                .balance(BigDecimal.valueOf(10000))
                .availableBalance(BigDecimal.valueOf(8000))
                .active(true)
                .currency(currency)
                .build();

        return List.of(account1, account2);
    }
}
