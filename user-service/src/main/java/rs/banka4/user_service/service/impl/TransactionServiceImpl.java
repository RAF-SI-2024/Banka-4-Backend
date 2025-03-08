package rs.banka4.user_service.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.domain.transaction.dtos.TransactionDto;
import rs.banka4.user_service.domain.transaction.db.TransactionStatus;
import rs.banka4.user_service.domain.transaction.dtos.CreatePaymentDto;
import rs.banka4.user_service.domain.transaction.mapper.TransactionMapper;
import rs.banka4.user_service.exceptions.*;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.domain.transaction.db.MonetaryAmount;
import rs.banka4.user_service.domain.transaction.db.Transaction;
import rs.banka4.user_service.repositories.AccountRepository;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.repositories.TransactionRepository;
import rs.banka4.user_service.service.abstraction.TransactionService;
import rs.banka4.user_service.utils.JwtUtil;
import rs.banka4.user_service.utils.specification.PaymentSpecification;
import rs.banka4.user_service.utils.specification.SpecificationCombinator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final TransactionRepository transactionRepository;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public TransactionDto createPayment(Authentication authentication, CreatePaymentDto createPaymentDto) {
        String email = jwtUtil.extractUsername(authentication.getCredentials().toString());

        Client client = clientRepository.findByEmail(email).orElseThrow(() -> new UserNotFound(email));
        Account fromAccount = accountRepository.findAccountByAccountNumber(createPaymentDto.fromAccount()).orElseThrow(AccountNotFound::new);
        Account toAccount = accountRepository.findAccountByAccountNumber(createPaymentDto.toAccount()).orElseThrow(AccountNotFound::new);

        if (!client.getAccounts().contains(fromAccount)) {
            throw new NotAccountOwner();
        }
        if (fromAccount.getBalance().subtract(createPaymentDto.fromAmount()).subtract(BigDecimal.ONE).compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFunds();
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(createPaymentDto.fromAmount()).subtract(BigDecimal.ONE));
        toAccount.setBalance(toAccount.getBalance().add(createPaymentDto.fromAmount()));

        Transaction transaction = Transaction.builder()
                .transactionNumber(UUID.randomUUID().toString())
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .from(new MonetaryAmount(createPaymentDto.fromAmount(), fromAccount.getCurrency()))
                .to(new MonetaryAmount(createPaymentDto.fromAmount(), toAccount.getCurrency()))
                .fee(new MonetaryAmount(BigDecimal.valueOf(1L), fromAccount.getCurrency()))
                .recipient(createPaymentDto.recipient())
                .paymentCode(createPaymentDto.paymentCode())
                .referenceNumber(createPaymentDto.referenceNumber())
                .paymentPurpose(createPaymentDto.paymentPurpose())
                .paymentDateTime(LocalDateTime.now())
                .status(TransactionStatus.IN_PROGRESS)
                .build();

        transactionRepository.save(transaction);
        // TODO ne vraca se transactionDto kako je napisano za povratnu vrednost
        // TODO Da li tek treba da usledi task za prebacivanje novca, jer samo vidimo in progress
        return TransactionMapper.INSTANCE.toDto(transaction);
    }

    @Override
    @Transactional
    public TransactionDto createTransfer(Authentication authentication, CreatePaymentDto createPaymentDto) {
        String email = jwtUtil.extractUsername(authentication.getCredentials().toString());

        Client client = clientRepository.findByEmail(email).orElseThrow(() -> new UserNotFound(email));
        Account fromAccount = accountRepository.findAccountByAccountNumber(createPaymentDto.fromAccount()).orElseThrow(AccountNotFound::new);
        Account toAccount = accountRepository.findAccountByAccountNumber(createPaymentDto.toAccount()).orElseThrow(AccountNotFound::new);

        if (!client.getAccounts().containsAll(Set.of(fromAccount, toAccount))) {
            throw new NotAccountOwner();
        }
        if (fromAccount.getBalance().subtract(createPaymentDto.fromAmount()).compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFunds();
        }

        // TODO: handle in future exchange rates and reserved amounts
        fromAccount.setBalance(fromAccount.getBalance().subtract(createPaymentDto.fromAmount()));
        toAccount.setBalance(toAccount.getBalance().add(createPaymentDto.fromAmount()));

        Transaction transaction = Transaction.builder()
                .transactionNumber(UUID.randomUUID().toString())
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .from(new MonetaryAmount(createPaymentDto.fromAmount(), fromAccount.getCurrency()))
                .to(new MonetaryAmount(createPaymentDto.fromAmount(), toAccount.getCurrency()))
                .fee(new MonetaryAmount(BigDecimal.valueOf(0L), fromAccount.getCurrency()))
                .recipient(createPaymentDto.recipient())
                .paymentCode(createPaymentDto.paymentCode())
                .referenceNumber(createPaymentDto.referenceNumber())
                .paymentPurpose(createPaymentDto.paymentPurpose())
                .paymentDateTime(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);
        // TODO ne vraca se transactionDto kako je napisano za povratnu vrednost i sto nema status in progress
        return TransactionMapper.INSTANCE.toDto(transaction);
    }

    @Override
    public Page<TransactionDto> getAllTransactionsForClient(String token, TransactionStatus paymentStatus, BigDecimal amount, LocalDate paymentDate, String accountNumber, PageRequest pageRequest) {
        SpecificationCombinator<Transaction> combinator = new SpecificationCombinator<>();

        if (paymentStatus != null) combinator.and(PaymentSpecification.hasStatus(paymentStatus));
        if (amount != null) combinator.and(PaymentSpecification.hasAmount(amount));
        if (paymentDate != null) combinator.and(PaymentSpecification.hasDate(paymentDate));

        if (accountNumber != null && !accountNumber.isEmpty()) {
            Account fromAccount = accountRepository.findAccountByAccountNumber(accountNumber)
                    .orElseThrow(AccountNotFound::new);

            combinator.and(PaymentSpecification.hasFromAccount(fromAccount));
            combinator.and(PaymentSpecification.hasToAccount(fromAccount));
        }

        Page<Transaction> transactions = transactionRepository.findAll(combinator.build(), pageRequest);

        return transactions.map(TransactionMapper.INSTANCE::toDto);
    }

    @Override
    public TransactionDto getTransactionById(String token, UUID transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFound(transactionId.toString()));

        //TODO: check if user is owner of transaction

        return TransactionMapper.INSTANCE.toDto(transaction);
    }

}