package rs.banka4.user_service.unit.loan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import rs.banka4.rafeisen.common.currency.CurrencyCode;
import rs.banka4.rafeisen.common.exceptions.jwt.Unauthorized;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.currency.db.Currency;
import rs.banka4.user_service.domain.loan.db.Loan;
import rs.banka4.user_service.domain.loan.db.LoanStatus;
import rs.banka4.user_service.exceptions.loan.LoanAlreadyJudged;
import rs.banka4.user_service.exceptions.loan.LoanNotFound;
import rs.banka4.user_service.repositories.AccountRepository;
import rs.banka4.user_service.repositories.LoanInstallmentRepository;
import rs.banka4.user_service.repositories.LoanRepository;
import rs.banka4.user_service.service.abstraction.JwtService;
import rs.banka4.user_service.service.impl.BankAccountServiceImpl;
import rs.banka4.user_service.service.impl.LoanServiceImpl;
import rs.banka4.user_service.service.impl.TransactionServiceImpl;

public class ManageLoansTests {
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Mock
    private LoanRepository loanRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private LoanInstallmentRepository loanInstallmentRepository;
    @Mock
    private BankAccountServiceImpl bankAccountService;
    @Mock
    private TransactionServiceImpl transactionService;
    @Mock
    private AccountRepository accountRepository;
    @InjectMocks
    private LoanServiceImpl loanService;

    @Test
    void unauthorizedJwt_approveLoan() {
        when(jwtService.extractRole("jwt")).thenReturn("client");

        assertThrows(Unauthorized.class, () -> loanService.approveLoan(123L, "jwt"));
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void unauthorizedJwt_rejectLoan() {
        when(jwtService.extractRole("jwt")).thenReturn("client");

        assertThrows(Unauthorized.class, () -> loanService.rejectLoan(123L, "jwt"));
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void approveLoan_loanStatusBad() {
        Long loanNumber = 123L;
        Loan loan = new Loan();
        loan.setRepaymentPeriod(3);
        loan.setStatus(LoanStatus.APPROVED);
        loan.setLoanNumber(loanNumber);

        when(jwtService.extractRole("jwt")).thenReturn("employee");

        when(loanRepository.findByLoanNumber(loanNumber)).thenReturn(Optional.of(loan));
        when(loanRepository.save(loan)).thenReturn(loan);


        assertThrows(LoanAlreadyJudged.class, () -> loanService.approveLoan(loanNumber, "jwt"));
        verify(loanRepository, never()).save(loan);
    }

    @Test
    void rejectLoan_loanStatusBad() {
        Long loanNumber = 123L;
        Loan loan = new Loan();
        loan.setRepaymentPeriod(3);
        loan.setStatus(LoanStatus.APPROVED);
        loan.setLoanNumber(loanNumber);

        when(jwtService.extractRole("jwt")).thenReturn("employee");

        when(loanRepository.findByLoanNumber(loanNumber)).thenReturn(Optional.of(loan));
        when(loanRepository.save(loan)).thenReturn(loan);


        assertThrows(LoanAlreadyJudged.class, () -> loanService.rejectLoan(loanNumber, "jwt"));
        verify(loanRepository, never()).save(loan);
    }

    @Test
    void approveLoan_success() {
        Long loanNumber = 123L;
        Loan loan = new Loan();
        loan.setRepaymentPeriod(3);
        loan.setLoanNumber(loanNumber);
        loan.setStatus(LoanStatus.PROCESSING);
        loan.setAmount(new BigDecimal("1000"));

        Account userAccount = new Account();
        Currency currency = new Currency();
        currency.setCode(CurrencyCode.Code.RSD);
        userAccount.setCurrency(currency);
        userAccount.setBalance(new BigDecimal("5000"));
        loan.setAccount(userAccount);

        Account bankAccount = new Account();
        bankAccount.setBalance(new BigDecimal("10000"));

        when(jwtService.extractRole("jwt")).thenReturn("employee");
        when(loanRepository.findByLoanNumber(loanNumber)).thenReturn(Optional.of(loan));
        when(loanRepository.save(loan)).thenReturn(loan);
        when(bankAccountService.getBankAccountForCurrency(currency.getCode())).thenReturn(
            bankAccount
        );

        loanService.approveLoan(loanNumber, "jwt");

        assertEquals(loan.getAgreementDate(), LocalDate.now());
        assertEquals(
            loan.getNextInstallmentDate(),
            LocalDate.now()
                .plusMonths(1)
        );
        assertEquals(
            loan.getDueDate(),
            LocalDate.now()
                .plusMonths(3)
        );
        assertEquals(LoanStatus.APPROVED, loan.getStatus());
        verify(loanRepository).save(loan);
        verify(accountRepository).save(bankAccount);
        verify(accountRepository).save(userAccount);
        verify(transactionService).createBankTransferTransaction(
            bankAccount,
            userAccount,
            loan.getAmount(),
            "Loan disbursement"
        );
    }

    @Test
    void approveLoan_notFound() {
        Long loanNumber = 123L;
        when(loanRepository.findByLoanNumber(loanNumber)).thenReturn(Optional.empty());
        when(jwtService.extractRole("jwt")).thenReturn("employee");

        assertThrows(LoanNotFound.class, () -> loanService.approveLoan(loanNumber, "jwt"));
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void rejectLoan_success() {
        Long loanNumber = 456L;
        Loan loan = new Loan();
        loan.setLoanNumber(loanNumber);
        loan.setStatus(LoanStatus.PROCESSING);

        when(jwtService.extractRole("jwt")).thenReturn("employee");
        when(loanRepository.findByLoanNumber(loanNumber)).thenReturn(Optional.of(loan));
        when(loanRepository.save(loan)).thenReturn(loan);

        loanService.rejectLoan(loanNumber, "jwt");

        assertEquals(LoanStatus.REJECTED, loan.getStatus());
        verify(loanRepository).save(loan);
    }

    @Test
    void rejectLoan_notFound() {
        Long loanNumber = 456L;

        when(jwtService.extractRole("jwt")).thenReturn("employee");
        when(loanRepository.findByLoanNumber(loanNumber)).thenReturn(Optional.empty());

        assertThrows(LoanNotFound.class, () -> loanService.rejectLoan(loanNumber, "jwt"));
        verify(loanRepository, never()).save(any(Loan.class));
    }
}
