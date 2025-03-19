package rs.banka4.user_service.integration.seeder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.card.db.Card;
import rs.banka4.user_service.domain.card.db.CardName;
import rs.banka4.user_service.domain.card.db.CardStatus;
import rs.banka4.user_service.domain.card.db.CardType;
import rs.banka4.user_service.domain.currency.db.Currency;
import rs.banka4.user_service.domain.loan.db.Loan;
import rs.banka4.user_service.domain.loan.db.LoanStatus;
import rs.banka4.user_service.domain.loan.db.LoanType;
import rs.banka4.user_service.repositories.*;

@Component
public class TestDataSeeder {

    @Autowired
    private CurrencyRepository currencyRepository;

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



    /**
     * Kreira i čuva valutu u bazi.
     * @return Sačuvani Currency objekat.
     */
    public Currency seedCurrency() {
        Currency currency = Currency.builder()
                .name("Euro")
                .description("Euro currency")
                .symbol("€")
                .code(Currency.Code.EUR)
                .active(true)
                .build();
        return currencyRepository.saveAndFlush(currency);
    }

    /**
     * Kreira i čuva nalog u bazi sa podrazumevanom valutom.
     * @return Sačuvani Account objekat.
     */
    public Account seedAccount() {
        Currency currency = seedCurrency();

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

    /**
     * Kreira i čuva kredit u bazi.
     * @return Sačuvani Loan objekat.
     */
    public Loan seedLoan(Account account) {
        return loanRepository.saveAndFlush(
                Loan.builder()
                        .loanNumber(100001L)
                        .amount(BigDecimal.valueOf(5000))
                        .repaymentPeriod(36)
                        .agreementDate(LocalDate.now())
                        .dueDate(LocalDate.now().plusYears(3))
                        .monthlyInstallment(BigDecimal.valueOf(150))
                        .nextInstallmentDate(LocalDate.now().plusMonths(1))
                        .remainingDebt(BigDecimal.valueOf(4500))
                        .baseInterestRate(BigDecimal.valueOf(3.5))
                        .account(account)
                        .status(LoanStatus.PROCESSING)
                        .type(LoanType.CASH)
                        .interestType(Loan.InterestType.FIXED)
                        .build()
        );
    }
    /**
     * Kreira i čuva 2 kredita u bazi.
     * @return Listu sačuvanih Loans objekata.
     */
    public List<Loan> seedLoans(Account account) {
        Loan loan1 = loanRepository.saveAndFlush(
                Loan.builder()
                        .loanNumber(100001L)
                        .amount(BigDecimal.valueOf(5000))
                        .repaymentPeriod(36)
                        .agreementDate(LocalDate.now())
                        .dueDate(LocalDate.now().plusYears(3))
                        .monthlyInstallment(BigDecimal.valueOf(150))
                        .nextInstallmentDate(LocalDate.now().plusMonths(1))
                        .remainingDebt(BigDecimal.valueOf(4500))
                        .baseInterestRate(BigDecimal.valueOf(3.5))
                        .account(account)
                        .status(LoanStatus.APPROVED)
                        .type(LoanType.CASH)
                        .interestType(Loan.InterestType.FIXED)
                        .build()
        );

        Loan loan2 = loanRepository.saveAndFlush(
                Loan.builder()
                        .loanNumber(100002L)
                        .amount(BigDecimal.valueOf(10000))
                        .repaymentPeriod(48)
                        .agreementDate(LocalDate.now().minusMonths(3))
                        .dueDate(LocalDate.now().plusYears(4))
                        .monthlyInstallment(BigDecimal.valueOf(250))
                        .nextInstallmentDate(LocalDate.now().plusMonths(1))
                        .remainingDebt(BigDecimal.valueOf(8000))
                        .baseInterestRate(BigDecimal.valueOf(4.0))
                        .account(account)
                        .status(LoanStatus.PAID_OFF)
                        .type(LoanType.CASH)
                        .interestType(Loan.InterestType.VARIABLE)
                        .build()
        );

        return List.of(loan1, loan2);
    }
    /**
     * Kreira i čuva karticu u bazi.
     * @return Karticu sačuvan Card objekat.
     */
    public Card seedCard(Account account) {
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
                        .expiresAt(LocalDate.now().plusYears(5))
                        .build()
        );
    }
    /**
     * Creates and saves two accounts in the database.
     * @return List of two saved Account objects.
     */
    public List<Account> seedTwoAccounts() {
        Currency currency = seedCurrency();
        Account account1 = seedAccount();
        Account account2 = Account.builder()
                .accountNumber("0987654321")
                .balance(BigDecimal.valueOf(10000))
                .availableBalance(BigDecimal.valueOf(8000))
                .active(true)
                .currency(currency)
                .build();

        return List.of(account1, account2);
    }
}
