package rs.banka4.user_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.dto.PaymentStatus;
import rs.banka4.user_service.exceptions.InsufficientFunds;
import rs.banka4.user_service.exceptions.NotFound;
import rs.banka4.user_service.exceptions.TransactionInvalidOrAlreadyProcessed;
import rs.banka4.user_service.models.Account;
import rs.banka4.user_service.models.AuthenticationEvent;
import rs.banka4.user_service.models.Transaction;
import rs.banka4.user_service.repositories.AccountRepository;
import rs.banka4.user_service.repositories.TransactionRepository;
import rs.banka4.user_service.service.abstraction.PaymentVerificationProcessor;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentTransactionVerificationProcessor implements PaymentVerificationProcessor {

    private final TransactionRepository transactionRepository;

    @Override
    public void process(AuthenticationEvent event) {
        Transaction transaction = transactionRepository.findByTransactionNumber(event.getEventId())
                .orElseThrow(NotFound::new);

        if (!transaction.getStatus().equals(PaymentStatus.IN_PROGRESS)) {
            throw new TransactionInvalidOrAlreadyProcessed();
        }

        Account fromAccount = transaction.getFromAccount();
        Account toAccount = transaction.getToAccount();

        // TODO: Move this to banks account
        // Transfers include the 1 unit fee
        BigDecimal fee = BigDecimal.ONE;
        BigDecimal totalDebit = transaction.getFrom().getAmount().add(fee);

        if (fromAccount.getBalance().compareTo(totalDebit) < 0) {
            transaction.setStatus(PaymentStatus.REJECTED);
            transactionRepository.save(transaction);

            throw new InsufficientFunds();
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(totalDebit));
        toAccount.setBalance(toAccount.getBalance().add(transaction.getFrom().getAmount()));

        transaction.setStatus(PaymentStatus.REALIZED);

        transactionRepository.save(transaction);
    }
}
