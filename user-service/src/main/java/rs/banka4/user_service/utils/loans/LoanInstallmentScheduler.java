package rs.banka4.user_service.utils.loans;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.banka4.user_service.config.RabbitMqConfig;
import rs.banka4.user_service.domain.auth.dtos.NotificationTransferDto;
import rs.banka4.user_service.domain.loan.db.LoanInstallment;
import rs.banka4.user_service.domain.loan.db.LoanStatus;
import rs.banka4.user_service.domain.loan.db.PaymentStatus;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.repositories.AccountRepository;
import rs.banka4.user_service.repositories.LoanInstallmentRepository;
import rs.banka4.user_service.repositories.LoanRepository;
import rs.banka4.user_service.utils.MessageHelper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanInstallmentScheduler {

    private final LoanInstallmentRepository loanInstallmentRepository;
    private final LoanRepository loanRepository;
    private final AccountRepository accountRepository;
    private final RabbitTemplate rabbitTemplate;
    private final MessageHelper messageHelper;

    private static final BigDecimal LATE_PAYMENT_PENALTY = new BigDecimal("0.05");
    private static final BigDecimal LEGAL_THRESHOLD = new BigDecimal("1000");

    /**
     * Daily job at 1 AM: Process due installments.
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void processDueInstallments() {
        List<LoanInstallment> dueInstallments = loanInstallmentRepository.findByExpectedDueDateAndPaymentStatus(LocalDate.now(), PaymentStatus.UNPAID);

        for (LoanInstallment installment : dueInstallments) {
            payInstallmentIfPossible(installment);
        }
    }

    /**
     * Retries `DELAYED` installments every 6 hours for up to 72 hours.
     */
    @Scheduled(cron = "0 0 */6 * * ?")
    @Transactional
    public void retryDelayedInstallments() {
        LocalDate overdueThreshold = LocalDate.now().minusDays(3);
        List<LoanInstallment> delayedInstallments = loanInstallmentRepository.findByPaymentStatusAndExpectedDueDate(PaymentStatus.DELAYED, overdueThreshold);

        for (LoanInstallment installment : delayedInstallments) {
            payInstallmentIfPossible(installment);
        }
    }

    /**
     * Daily at midnight: Apply penalties for `DELAYED` installments after 72 hours.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void applyLatePaymentPenalties() {
        LocalDate overdueThreshold = LocalDate.now().minusDays(3);
        List<LoanInstallment> delayedInstallments = loanInstallmentRepository.findByPaymentStatusAndExpectedDueDate(PaymentStatus.DELAYED, overdueThreshold);

        for (LoanInstallment installment : delayedInstallments) {
            // Apply penalty
            BigDecimal newInterestRate = installment.getLoan().getInterestRate().getFixedRate().add(LATE_PAYMENT_PENALTY);
            installment.setInterestRateAmount(newInterestRate);
            installment.getLoan().getInterestRate().setFixedRate(newInterestRate);
            loanInstallmentRepository.save(installment);

            //Message for applied penalty
            NotificationTransferDto message = MessageHelper.createLoanInstallmentPenaltyMessage(
                    installment.getLoan().getAccount().getClient().email,
                    installment.getLoan().getAccount().getClient().firstName,
                    installment.getLoan().getLoanNumber(),
                    LATE_PAYMENT_PENALTY,
                    LocalDate.now()
            );
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.EXCHANGE_NAME,
                    RabbitMqConfig.ROUTING_KEY,
                    message
            );

            // Check if total overdue exceeds legal threshold
            if (installment.getLoan().getRemainingDebt().compareTo(LEGAL_THRESHOLD) > 0) {
                //TODO: logic for handling exceeding legal threshold
            }
        }
    }

    private void payInstallmentIfPossible(LoanInstallment installment){
        Account account = installment.getLoan().getAccount();
        BigDecimal installmentAmount = installment.getInstallmentAmount();

        if (account.getBalance().compareTo(installmentAmount) >= 0) {
            // Subtract from user account and paying installment
            account.setBalance(account.getBalance().subtract(installmentAmount));
            installment.getLoan().setRemainingDebt(installment.getLoan().getRemainingDebt().subtract(installmentAmount));

            // In case loan is paid off.
            if (installment.getLoan().getRemainingDebt().compareTo(BigDecimal.ZERO) == 0){
                installment.getLoan().setStatus(LoanStatus.PAID_OFF);
                installment.getLoan().setNextInstallmentDate(null);
            }

            installment.setPaymentStatus(PaymentStatus.PAID);
            installment.setActualDueDate(LocalDate.now());
            loanInstallmentRepository.save(installment);
            accountRepository.save(account);
            loanRepository.save(installment.getLoan());

            // Message for successful payment
            NotificationTransferDto message = MessageHelper.createLoanInstallmentPayedMessage(
                    account.getClient().email,
                    account.getClient().firstName,
                    installment.getLoan().getLoanNumber(),
                    installment.getInstallmentAmount(),
                    LocalDate.now()
            );
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.EXCHANGE_NAME,
                    RabbitMqConfig.ROUTING_KEY,
                    message
            );
        } else {
            // Mark as delayed
            installment.setPaymentStatus(PaymentStatus.DELAYED);
            loanInstallmentRepository.save(installment);

            // Message for denied payment.
            NotificationTransferDto message = MessageHelper.createLoanInstallmentPaymentDeniedMessage(
                    account.getClient().email,
                    account.getClient().firstName,
                    installment.getLoan().getLoanNumber(),
                    installment.getInstallmentAmount(),
                    LocalDate.now()
            );
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.EXCHANGE_NAME,
                    RabbitMqConfig.ROUTING_KEY,
                    message
            );
        }
    }
}
