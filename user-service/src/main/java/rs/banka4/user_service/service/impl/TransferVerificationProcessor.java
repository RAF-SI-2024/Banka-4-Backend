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
public class TransferVerificationProcessor implements PaymentVerificationProcessor {

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
        BigDecimal amount = transaction.getFrom().getAmount();

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            transaction.setStatus(PaymentStatus.REJECTED);
            transactionRepository.save(transaction);

            throw new InsufficientFunds();
        }

        // Update account balances (no fee for transfers)
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        transaction.setStatus(PaymentStatus.REALIZED);
        transactionRepository.save(transaction);
    }
}
