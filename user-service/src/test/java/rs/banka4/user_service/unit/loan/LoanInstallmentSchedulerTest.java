package rs.banka4.user_service.unit.loan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.auth.dtos.NotificationTransferDto;
import rs.banka4.user_service.domain.currency.db.Currency;
import rs.banka4.user_service.domain.loan.db.*;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.repositories.AccountRepository;
import rs.banka4.user_service.repositories.LoanInstallmentRepository;
import rs.banka4.user_service.repositories.LoanRepository;
import rs.banka4.user_service.utils.loans.LoanRateUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LoanInstallmentSchedulerTest {

    @Mock
    private LoanInstallmentRepository loanInstallmentRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private LoanRateUtil loanRateUtil;

    @InjectMocks
    private rs.banka4.user_service.utils.loans.LoanInstallmentScheduler loanInstallmentScheduler;

    private Account account;
    private Loan loan;
    private LoanInstallment installment;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setBalance(new BigDecimal("5000"));

        Currency currency = new Currency();
        currency.setCode(Currency.Code.RSD);  // Use the appropriate currency code
        account.setCurrency(currency); // Ensure account has a currency


        Client client = new Client();
        client.setEmail("lazar.vuksanovic17@gmail.com");
        client.setFirstName("John");
        account.setClient(client);

        InterestRate interestRate = new InterestRate();
        interestRate.setFixedRate(new BigDecimal("0.05"));

        loan = new Loan();
        loan.setId(UUID.randomUUID());
        loan.setRemainingDebt(new BigDecimal("1000"));
        loan.setMonthlyInstallment(new BigDecimal("1000"));
        loan.setAccount(account);
        loan.setStatus(LoanStatus.APPROVED);
        loan.setInterestRate(interestRate);
        loan.setBaseInterestRate(new BigDecimal("0.05"));

        installment = new LoanInstallment();
        installment.setId(UUID.randomUUID());
        installment.setLoan(loan);
        installment.setInstallmentAmount(new BigDecimal("1000"));
        installment.setPaymentStatus(PaymentStatus.DELAYED);
        installment.setExpectedDueDate(LocalDate.now().minusDays(3));
    }



    @Test
    void testProcessDueInstallments_ShouldPayInstallmentIfPossible() {
        when(loanInstallmentRepository.findByExpectedDueDateAndPaymentStatus(LocalDate.now(), PaymentStatus.UNPAID))
                .thenReturn(List.of(installment));

        loanInstallmentScheduler.processDueInstallments();

        assertEquals(BigDecimal.ZERO, loan.getRemainingDebt());
        assertEquals(PaymentStatus.PAID, installment.getPaymentStatus());
        assertEquals(LoanStatus.PAID_OFF, loan.getStatus());
        verify(accountRepository).save(account);
        verify(loanRepository).save(loan);
        verify(loanInstallmentRepository).save(installment);
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(NotificationTransferDto.class));
    }

    @Test
    void testRetryDelayedInstallments_ShouldRetryIfDelayed() {
        when(loanInstallmentRepository.findByPaymentStatusAndExpectedDueDate(PaymentStatus.DELAYED, LocalDate.now().minusDays(3)))
                .thenReturn(List.of(installment));

        loanInstallmentScheduler.retryDelayedInstallments();

        assertEquals(BigDecimal.ZERO, loan.getRemainingDebt());
        assertEquals(PaymentStatus.PAID, installment.getPaymentStatus());
        verify(loanInstallmentRepository).save(installment);
    }

    @Test
    void testApplyLatePaymentPenalties_ShouldApplyPenalty() {
        when(loanInstallmentRepository.findByPaymentStatusAndExpectedDueDate(PaymentStatus.DELAYED, LocalDate.now().minusDays(3)))
                .thenReturn(List.of(installment));

        loanInstallmentScheduler.applyLatePaymentPenalties();

        // Capture the saved installment
        ArgumentCaptor<LoanInstallment> captor = ArgumentCaptor.forClass(LoanInstallment.class);
        verify(loanInstallmentRepository).save(captor.capture());
        LoanInstallment savedInstallment = captor.getValue();

        assertNotNull(savedInstallment.getLoan(), "Loan should not be null after penalty is applied");

        // Ensure interest rate increases
        assertTrue(savedInstallment.getLoan().getBaseInterestRate().compareTo(new BigDecimal("0.05")) > 0,
                "Base interest rate should increase due to penalty");

        verify(loanRepository).save(savedInstallment.getLoan());
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(NotificationTransferDto.class));
    }
}