package rs.banka4.user_service.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.transaction.db.MonetaryAmount;
import rs.banka4.user_service.domain.transaction.db.Transaction;
import rs.banka4.user_service.domain.transaction.db.TransactionStatus;
import rs.banka4.user_service.domain.transaction.dtos.CreatePaymentDto;
import rs.banka4.user_service.domain.transaction.dtos.TransactionDto;
import rs.banka4.user_service.domain.transaction.mapper.TransactionMapper;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.domain.user.client.db.ClientContact;
import rs.banka4.user_service.exceptions.account.AccountNotActive;
import rs.banka4.user_service.exceptions.account.AccountNotFound;
import rs.banka4.user_service.exceptions.account.NotAccountOwner;
import rs.banka4.user_service.exceptions.authenticator.NotValidTotpException;
import rs.banka4.user_service.exceptions.transaction.InsufficientFunds;
import rs.banka4.user_service.exceptions.transaction.TransactionNotFound;
import rs.banka4.user_service.exceptions.user.UserNotFound;
import rs.banka4.user_service.repositories.AccountRepository;
import rs.banka4.user_service.repositories.ClientContactRepository;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.repositories.TransactionRepository;
import rs.banka4.user_service.service.abstraction.TransactionService;
import rs.banka4.user_service.utils.JwtUtil;
import rs.banka4.user_service.utils.specification.PaymentSpecification;
import rs.banka4.user_service.utils.specification.SpecificationCombinator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final TransactionRepository transactionRepository;
    private final ClientContactRepository clientContactRepository;
    private final TotpService totpService;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public TransactionDto createTransaction(Authentication authentication, CreatePaymentDto createPaymentDto) {
        Client client = getClient(authentication);

        if (!veifyClient(authentication, createPaymentDto.otpCode())) {
            throw new NotValidTotpException();
        }

        Account fromAccount = getAccount(createPaymentDto.fromAccount());
        Account toAccount = getAccount(createPaymentDto.toAccount());

        validateAccountActive(fromAccount);
        validateClientAccountOwnership(client, fromAccount);
        validateSufficientFunds(fromAccount, createPaymentDto.fromAmount().add(BigDecimal.ONE));

        processTransaction(fromAccount, toAccount, createPaymentDto.fromAmount(), BigDecimal.ONE);

        Transaction transaction = buildTransaction(fromAccount, toAccount, createPaymentDto, BigDecimal.ONE, TransactionStatus.REALIZED);

        if (createPaymentDto.saveRecipient()) {
            ClientContact clientContact = ClientContact.builder()
                    .client(client)
                    .accountNumber(toAccount.getAccountNumber())
                    .nickname(createPaymentDto.recipient())
                    .build();

            clientContactRepository.save(clientContact);
        }

        transactionRepository.save(transaction);

        return TransactionMapper.INSTANCE.toDto(transaction);
    }

    @Override
    @Transactional
    public TransactionDto createTransfer(Authentication authentication, CreatePaymentDto createPaymentDto) {
        Client client = getClient(authentication);

        if (!veifyClient(authentication, createPaymentDto.otpCode())) {
            throw new NotValidTotpException();
        }

        Account fromAccount = getAccount(createPaymentDto.fromAccount());
        Account toAccount = getAccount(createPaymentDto.toAccount());

        validateAccountActive(fromAccount);
        validateClientAccountOwnership(client, fromAccount, toAccount);
        validateSufficientFunds(fromAccount, createPaymentDto.fromAmount());

        processTransaction(fromAccount, toAccount, createPaymentDto.fromAmount(), BigDecimal.ZERO);

        Transaction transaction = buildTransaction(fromAccount, toAccount, createPaymentDto, BigDecimal.ZERO, TransactionStatus.REALIZED);

        if (createPaymentDto.saveRecipient()) {
            ClientContact clientContact = ClientContact.builder()
                    .client(client)
                    .accountNumber(toAccount.getAccountNumber())
                    .nickname(createPaymentDto.recipient())
                    .build();

            clientContactRepository.save(clientContact);
        }

        transactionRepository.save(transaction);

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

            combinator.or(PaymentSpecification.hasFromAccount(fromAccount));
            combinator.or(PaymentSpecification.hasToAccount(fromAccount));
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

    private Client getClient(Authentication authentication) {
        String email = jwtUtil.extractUsername(authentication.getCredentials().toString());
        return clientRepository.findByEmail(email).orElseThrow(() -> new UserNotFound(email));
    }

    private Account getAccount(String accountNumber) {
        return accountRepository.findAccountByAccountNumber(accountNumber).orElseThrow(AccountNotFound::new);
    }

    private void validateClientAccountOwnership(Client client, Account... accounts) {
        for (Account account : accounts) {
            if (!client.getAccounts().contains(account)) {
                throw new NotAccountOwner();
            }
        }
    }

    private void validateSufficientFunds(Account fromAccount, BigDecimal amount) {
        if (fromAccount.getBalance().subtract(amount).compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFunds();
        }
    }

    private boolean veifyClient(Authentication authentication, String otpCode) {
        return totpService.validate(authentication.getCredentials().toString(), otpCode);
    }

    private void validateAccountActive(Account account) {
        boolean isActive = account.isActive();
        if (!isActive) {
            throw new AccountNotActive();
        }
    }

    private void processTransaction(Account fromAccount, Account toAccount, BigDecimal amount, BigDecimal fee) {
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount).subtract(fee));
        toAccount.setBalance(toAccount.getBalance().add(amount));
    }

    private Transaction buildTransaction(Account fromAccount, Account toAccount, CreatePaymentDto createPaymentDto, BigDecimal fee, TransactionStatus status) {
        return Transaction.builder()
                .transactionNumber(UUID.randomUUID().toString())
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .from(new MonetaryAmount(createPaymentDto.fromAmount(), fromAccount.getCurrency()))
                .to(new MonetaryAmount(createPaymentDto.fromAmount(), toAccount.getCurrency()))
                .fee(new MonetaryAmount(fee, fromAccount.getCurrency()))
                .recipient(createPaymentDto.recipient())
                .paymentCode(createPaymentDto.paymentCode())
                .referenceNumber(createPaymentDto.referenceNumber())
                .paymentPurpose(createPaymentDto.paymentPurpose())
                .paymentDateTime(LocalDateTime.now())
                .status(status)
                .build();
    }
}