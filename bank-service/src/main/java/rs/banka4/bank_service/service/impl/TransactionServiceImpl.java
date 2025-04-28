package rs.banka4.bank_service.service.impl;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import rs.banka4.bank_service.domain.account.db.Account;
import rs.banka4.bank_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;
import rs.banka4.bank_service.domain.transaction.db.Transaction;
import rs.banka4.bank_service.domain.transaction.db.TransactionStatus;
import rs.banka4.bank_service.domain.transaction.dtos.*;
import rs.banka4.bank_service.domain.transaction.mapper.TransactionMapper;
import rs.banka4.bank_service.domain.user.client.db.Client;
import rs.banka4.bank_service.domain.user.client.db.ClientContact;
import rs.banka4.bank_service.exceptions.account.AccountNotActive;
import rs.banka4.bank_service.exceptions.account.AccountNotFound;
import rs.banka4.bank_service.exceptions.account.NotAccountOwner;
import rs.banka4.bank_service.exceptions.authenticator.NotValidTotpException;
import rs.banka4.bank_service.exceptions.transaction.*;
import rs.banka4.bank_service.exceptions.user.NotFound;
import rs.banka4.bank_service.exceptions.user.UserNotFound;
import rs.banka4.bank_service.repositories.AccountRepository;
import rs.banka4.bank_service.repositories.ClientContactRepository;
import rs.banka4.bank_service.repositories.ClientRepository;
import rs.banka4.bank_service.repositories.TransactionRepository;
import rs.banka4.bank_service.service.abstraction.ExchangeRateService;
import rs.banka4.bank_service.service.abstraction.JwtService;
import rs.banka4.bank_service.service.abstraction.TotpService;
import rs.banka4.bank_service.service.abstraction.TransactionService;
import rs.banka4.bank_service.tx.data.DoubleEntryTransaction;
import rs.banka4.bank_service.tx.data.Posting;
import rs.banka4.bank_service.tx.data.TxAccount;
import rs.banka4.bank_service.tx.data.TxAsset;
import rs.banka4.bank_service.tx.executor.InterbankTxExecutor;
import rs.banka4.bank_service.utils.specification.PaymentSpecification;
import rs.banka4.rafeisen.common.currency.CurrencyCode;
import rs.banka4.rafeisen.common.utils.specification.SpecificationCombinator;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final TransactionRepository transactionRepository;
    private final ClientContactRepository clientContactRepository;
    private final TotpService totpService;
    private final ExchangeRateService exchangeRateService;
    private final BankAccountServiceImpl bankAccountServiceImpl;
    private final JwtService jwtService;
    private final InterbankTxExecutor txExecutor;

    @Override
    @Transactional
    public TransactionDto createTransaction(
        Authentication authentication,
        CreatePaymentDto createPaymentDto
    ) {
        Client client = getClient(authentication);

        if (!verifyClient(authentication, createPaymentDto.otpCode())) {
            throw new NotValidTotpException();
        }

        Account fromAccount =
            accountRepository.findAccountByAccountNumber(createPaymentDto.fromAccount())
                .orElseThrow(AccountNotFound::new);

        Account toAccount =
            accountRepository.findAccountByAccountNumber(createPaymentDto.toAccount())
                .orElse(null);

        if (
            toAccount != null
                && fromAccount.getClient()
                    .getId()
                    .equals(
                        toAccount.getClient()
                            .getId()
                    )
        ) {
            throw new ClientCannotPayToOwnAccount();
        }

        validateAccountActive(fromAccount);
        validateClientAccountOwnership(client, fromAccount);
        validateSufficientFunds(
            fromAccount,
            createPaymentDto.fromAmount()
                .add(BigDecimal.ONE)
        );
        validateDailyAndMonthlyLimit(fromAccount, createPaymentDto.fromAmount());

        CurrencyCode toCurrency =
            toAccount != null ? toAccount.getCurrency() : fromAccount.getCurrency();

        Transaction transaction =
            processTransaction(
                fromAccount,
                createPaymentDto.toAccount(),
                toCurrency,
                createPaymentDto
            );

        if (createPaymentDto.saveRecipient()) {
            ClientContact clientContact =
                ClientContact.builder()
                    .client(client)
                    .accountNumber(createPaymentDto.toAccount())
                    .nickname(createPaymentDto.recipient())
                    .build();

            clientContactRepository.save(clientContact);
        }

        return TransactionMapper.INSTANCE.toDto(transaction);
    }

    @Override
    @Transactional
    public TransactionDto createTransfer(
        Authentication authentication,
        CreateTransferDto createTransferDto
    ) {
        Client client = getClient(authentication);

        if (!verifyClient(authentication, createTransferDto.otpCode())) {
            throw new NotValidTotpException();
        }

        Account fromAccount =
            accountRepository.findAccountByAccountNumber(createTransferDto.fromAccount())
                .orElseThrow(AccountNotFound::new);
        Account toAccount =
            accountRepository.findAccountByAccountNumber(createTransferDto.toAccount())
                .orElseThrow(AccountNotFound::new);

        if (fromAccount.equals(toAccount)) throw new ClientCannotTransferToSameAccount();

        validateAccountActive(fromAccount);
        validateClientAccountOwnership(client, fromAccount, toAccount);
        validateSufficientFunds(fromAccount, createTransferDto.fromAmount());

        Transaction transaction =
            processTransaction(
                fromAccount,
                toAccount.getAccountNumber(),
                toAccount.getCurrency(),
                createTransferDto
            );

        return TransactionMapper.INSTANCE.toDto(transaction);
    }

    @Override
    public Page<TransactionDto> getAllTransactionsForClient(
        String token,
        TransactionStatus paymentStatus,
        BigDecimal amount,
        LocalDate paymentDate,
        String accountNumber,
        PageRequest pageRequest
    ) {
        SpecificationCombinator<Transaction> combinator = new SpecificationCombinator<>();

        if (paymentStatus != null) combinator.and(PaymentSpecification.hasStatus(paymentStatus));
        if (amount != null) combinator.and(PaymentSpecification.hasAmount(amount));
        if (paymentDate != null) combinator.and(PaymentSpecification.hasDate(paymentDate));

        if (accountNumber != null && !accountNumber.isEmpty()) {
            Account fromAccount =
                accountRepository.findAccountByAccountNumber(accountNumber)
                    .orElseThrow(AccountNotFound::new);

            combinator.or(PaymentSpecification.hasFromAccount(fromAccount));
            combinator.or(PaymentSpecification.hasToAccount(fromAccount));
        }

        Client client =
            clientRepository.findById(jwtService.extractUserId(token))
                .orElseThrow(NotFound::new);
        if (
            !bankAccountServiceImpl.getBankOwner()
                .equals(client)
        ) {
            combinator.and(PaymentSpecification.isNotSpecialTransaction());
        }

        combinator.and(PaymentSpecification.isNotTransfer());

        Page<Transaction> transactions =
            transactionRepository.findAll(combinator.build(), pageRequest);

        return transactions.map(TransactionMapper.INSTANCE::toDto);
    }

    @Override
    public TransactionDto getTransactionById(String token, UUID transactionId) {
        Transaction transaction =
            transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFound(transactionId.toString()));

        // TODO: check if user is owner of transaction

        return TransactionMapper.INSTANCE.toDto(transaction);
    }

    @Override
    public Page<TransactionDto> getAllTransfersForClient(String token, PageRequest pageRequest) {
        UUID clientId = jwtService.extractUserId(token);
        Client client =
            clientRepository.findById(clientId)
                .orElseThrow(() -> new UserNotFound(clientId.toString()));

        Page<Transaction> transactions =
            transactionRepository.findAllByFromAccount_ClientAndIsTransferTrue(client, pageRequest);

        List<Transaction> filteredTransactions =
            transactions.stream()
                .filter(
                    transaction -> !transaction.getReferenceNumber()
                        .startsWith("CONV-")
                        && !transaction.getReferenceNumber()
                            .startsWith("TRF-")
                        && !transaction.getReferenceNumber()
                            .startsWith("FEE-")
                )
                .sorted(
                    (t1, t2) -> t2.getPaymentDateTime()
                        .compareTo(t1.getPaymentDateTime())
                )
                .collect(Collectors.toList());

        return new PageImpl<>(filteredTransactions, pageRequest, filteredTransactions.size()).map(
            TransactionMapper.INSTANCE::toDto
        );
    }


    // Private methods
    private Client getClient(Authentication authentication) {
        UUID clientId =
            jwtService.extractUserId(
                authentication.getCredentials()
                    .toString()
            );
        return clientRepository.findById(clientId)
            .orElseThrow(() -> new UserNotFound(clientId.toString()));
    }

    private Account getAccount(String accountNumber) {
        return accountRepository.findAccountByAccountNumber(accountNumber)
            .orElseThrow(AccountNotFound::new);
    }

    private void validateClientAccountOwnership(Client client, Account... accounts) {
        for (Account account : accounts) {
            if (
                !client.getAccounts()
                    .contains(account)
            ) {
                throw new NotAccountOwner();
            }
        }
    }

    private void validateSufficientFunds(Account fromAccount, BigDecimal amount) {
        if (
            fromAccount.getBalance()
                .subtract(amount)
                .compareTo(BigDecimal.ZERO)
                < 0
        ) {
            throw new InsufficientFunds();
        }
    }

    private void validateDailyAndMonthlyLimit(Account fromAccount, BigDecimal amount) {
        BigDecimal dailyLimit = fromAccount.getDailyLimit();
        BigDecimal monthlyLimit = fromAccount.getMonthlyLimit();

        BigDecimal totalDailyTransactions =
            transactionRepository.getTotalDailyTransactions(
                fromAccount.getAccountNumber(),
                LocalDate.now()
            );
        BigDecimal totalMonthlyTransactions =
            transactionRepository.getTotalMonthlyTransactions(
                fromAccount.getAccountNumber(),
                LocalDate.now()
                    .getMonthValue()
            );

        if (
            totalDailyTransactions.add(amount)
                .compareTo(dailyLimit)
                > 0
        ) {
            throw new ExceededDailyLimit();
        }

        if (
            totalMonthlyTransactions.add(amount)
                .compareTo(monthlyLimit)
                > 0
        ) {
            throw new ExceededMonthlyLimit();
        }
    }

    private boolean verifyClient(Authentication authentication, String otpCode) {
        return totpService.validate(
            authentication.getCredentials()
                .toString(),
            otpCode
        );
    }

    private void validateAccountActive(Account account) {
        boolean isActive = account.isActive();
        if (!isActive) {
            throw new AccountNotActive();
        }
    }

    // Private methods
    private Transaction buildTransaction(
        Account fromAccount,
        String toAccountNumber,
        CurrencyCode toCurrency,
        CreatePaymentDto createPaymentDto,
        BigDecimal fee,
        TransactionStatus status
    ) {
        BigDecimal toAmount =
            convertCurrency(createPaymentDto.fromAmount(), fromAccount.getCurrency(), toCurrency);

        return Transaction.builder()
            .transactionNumber(
                UUID.randomUUID()
                    .toString()
            )
            .fromAccount(fromAccount.getAccountNumber())
            .toAccount(toAccountNumber)
            .from(new MonetaryAmount(createPaymentDto.fromAmount(), fromAccount.getCurrency()))
            .to(new MonetaryAmount(toAmount, toCurrency))
            .fee(new MonetaryAmount(fee, fromAccount.getCurrency()))
            .recipient(createPaymentDto.recipient())
            .paymentCode(createPaymentDto.paymentCode())
            .referenceNumber(createPaymentDto.referenceNumber())
            .paymentPurpose(createPaymentDto.paymentPurpose())
            .paymentDateTime(LocalDateTime.now())
            .status(status)
            .build();
    }

    private Transaction buildTransfer(
        Account fromAccount,
        String toAccountNumber,
        CurrencyCode toCurrency,
        CreateTransferDto createTransferDto,
        BigDecimal fee,
        TransactionStatus status
    ) {
        BigDecimal toAmount =
            convertCurrency(createTransferDto.fromAmount(), fromAccount.getCurrency(), toCurrency);

        return Transaction.builder()
            .transactionNumber(
                UUID.randomUUID()
                    .toString()
            )
            .fromAccount(fromAccount.getAccountNumber())
            .toAccount(toAccountNumber)
            .from(new MonetaryAmount(createTransferDto.fromAmount(), fromAccount.getCurrency()))
            .to(new MonetaryAmount(toAmount, toCurrency))
            .fee(new MonetaryAmount(fee, fromAccount.getCurrency()))
            .recipient(
                fromAccount.getClient()
                    .getFirstName()
            )
            .paymentCode("101")
            .referenceNumber(
                String.valueOf(
                    fromAccount.getClient()
                        .getId()
                )
            )
            .paymentPurpose("Internal")
            .paymentDateTime(LocalDateTime.now())
            .status(status)
            .build();
    }


    private void createFeeTransaction(
        Account fromAccount,
        String toAccountNumber,
        CurrencyCode toCurrency,
        BigDecimal fee,
        ForeignBankId id
    ) {
        BigDecimal toAmount = convertCurrency(fee, fromAccount.getCurrency(), toCurrency);

        Transaction feeTransaction =
            buildSpecialTransaction(
                fromAccount,
                toAccountNumber,
                toCurrency,
                fee,
                toAmount,
                fee,
                "Bank Fee",
                "289",
                "FEE-" + UUID.randomUUID(),
                "Transaction Fee"
            );
        feeTransaction.setExecutingTransaction(id);
        transactionRepository.save(feeTransaction);
    }

    public void createBankTransferTransaction(
        Account fromAccount,
        String toAccount,
        CurrencyCode toCurrency,
        BigDecimal amount,
        String purpose
    ) {
        BigDecimal toAmount = convertCurrency(amount, fromAccount.getCurrency(), toCurrency);

        Transaction transaction =
            buildSpecialTransaction(
                fromAccount,
                toAccount,
                toCurrency,
                amount,
                toAmount,
                BigDecimal.ZERO,
                "Bank Transfer",
                "290",
                "TRF-" + UUID.randomUUID(),
                purpose
            );
        transactionRepository.save(transaction);
    }

    private Transaction buildSpecialTransaction(
        Account fromAccount,
        String toAccountNumber,
        CurrencyCode toCurrency,
        BigDecimal fromAmount,
        BigDecimal toAmount,
        BigDecimal fee,
        String recipient,
        String paymentCode,
        String referenceNumber,
        String paymentPurpose
    ) {
        return Transaction.builder()
            .transactionNumber(
                UUID.randomUUID()
                    .toString()
            )
            .fromAccount(fromAccount.getAccountNumber())
            .toAccount(toAccountNumber)
            .from(new MonetaryAmount(fromAmount, fromAccount.getCurrency()))
            .to(new MonetaryAmount(toAmount, toCurrency))
            .fee(new MonetaryAmount(fee, fromAccount.getCurrency()))
            .recipient(recipient)
            .paymentCode(paymentCode)
            .referenceNumber(referenceNumber)
            .paymentPurpose(paymentPurpose)
            .paymentDateTime(LocalDateTime.now())
            .status(TransactionStatus.REALIZED)
            .build();
    }

    private boolean hasEnoughFunds(Account account, BigDecimal fee) {
        return account.getBalance()
            .subtract(fee)
            .compareTo(BigDecimal.ZERO)
            >= 0;
    }

    private BigDecimal convertCurrency(
        BigDecimal amount,
        CurrencyCode fromCurrency,
        CurrencyCode toCurrency
    ) {
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }
        if (!fromCurrency.equals(CurrencyCode.RSD) && !toCurrency.equals(CurrencyCode.RSD)) {
            BigDecimal toRsd =
                exchangeRateService.convertCurrency(amount, fromCurrency, CurrencyCode.RSD);
            return exchangeRateService.convertCurrency(toRsd, CurrencyCode.RSD, toCurrency);
        }
        return exchangeRateService.convertCurrency(amount, fromCurrency, toCurrency);
    }

    @Transactional
    protected void transferAmount(Account fromAccount, Account toAccount, BigDecimal amount) {
        fromAccount.setBalance(
            fromAccount.getBalance()
                .subtract(amount)
        );
        toAccount.setBalance(
            toAccount.getBalance()
                .add(amount)
        );

        fromAccount.setAvailableBalance(
            fromAccount.getAvailableBalance()
                .subtract(amount)
        );
        toAccount.setAvailableBalance(
            toAccount.getAvailableBalance()
                .add(amount)
        );
    }

    /**
     * Transfers an amount between two accounts with different currencies.
     * <p>
     * Used to handle transfers between two bank accounts in different currencies.
     * </p>
     *
     * @param bankAccount1 bank account from which the amount is transferred
     * @param bankAccount2 bank account to which the amount is transferred
     * @param amount amount to be transferred
     * @param converted converted amount in the target currency
     */
    @Transactional
    protected void transferAmountDifferentCurrencies(
        Account bankAccount1,
        Account bankAccount2,
        BigDecimal amount,
        BigDecimal converted
    ) {
        bankAccount1.setBalance(
            bankAccount1.getBalance()
                .subtract(amount)
        );
        bankAccount2.setBalance(
            bankAccount2.getBalance()
                .add(converted)
        );

        bankAccount1.setAvailableBalance(
            bankAccount1.getAvailableBalance()
                .subtract(amount)
        );
        bankAccount2.setAvailableBalance(
            bankAccount2.getAvailableBalance()
                .add(converted)
        );
    }

    @Transactional
    protected Transaction processTransaction(
        Account fromAccount,
        String toAccountNumber,
        CurrencyCode toCurrency,
        CreateTransactionDto createTransactionDto
    ) {
        BigDecimal fromAmount;
        String message;
        if (createTransactionDto instanceof CreatePaymentDto) {
            fromAmount = ((CreatePaymentDto) createTransactionDto).fromAmount();
            message = ((CreatePaymentDto) createTransactionDto).paymentPurpose();
        } else {
            fromAmount = ((CreateTransferDto) createTransactionDto).fromAmount();
            message = "Internal transfer";
        }

        Posting posting =
            new Posting(
                new TxAccount.Account(fromAccount.getAccountNumber()),
                fromAmount,
                new TxAsset.Monas(fromAccount.getCurrency())
            );

        List<Posting> postings =
            txExecutor.ensurePostingCurrency(
                posting,
                toCurrency,
                new TxAccount.Account(toAccountNumber)
            );

        DoubleEntryTransaction transaction =
            new DoubleEntryTransaction(postings, message, ForeignBankId.our(UUID.randomUUID()));

        Transaction tx;
        ForeignBankId id = txExecutor.submitTx(transaction);
        BigDecimal fee = exchangeRateService.calculateFee(fromAmount);

        if (createTransactionDto instanceof CreatePaymentDto) {
            tx =
                buildTransaction(
                    fromAccount,
                    toAccountNumber,
                    toCurrency,
                    (CreatePaymentDto) createTransactionDto,
                    fee,
                    TransactionStatus.IN_PROGRESS
                );
        } else {
            tx =
                buildTransfer(
                    fromAccount,
                    toAccountNumber,
                    toCurrency,
                    (CreateTransferDto) createTransactionDto,
                    fee,
                    TransactionStatus.IN_PROGRESS
                );
        }

        tx.setExecutingTransaction(id);
        transactionRepository.save(tx);

        createSpecialTransactions(fromAccount, toAccountNumber, toCurrency, fromAmount, fee, id);

        return tx;
    }

    private void createSpecialTransactions(
        Account fromAccount,
        String toAccountNumber,
        CurrencyCode toCurrency,
        BigDecimal amount,
        BigDecimal fee,
        ForeignBankId id
    ) {
        if (
            fromAccount.getCurrency()
                .equals(toCurrency)
        ) return;

        Account fromBankAccount =
            bankAccountServiceImpl.getBankAccountForCurrency(fromAccount.getCurrency());
        Account toBankAccount = bankAccountServiceImpl.getBankAccountForCurrency(toCurrency);
        createFeeTransaction(
            fromAccount,
            fromBankAccount.getAccountNumber(),
            fromBankAccount.getCurrency(),
            fee,
            id
        );

        BigDecimal convertedAmount;

        if (fromAccount.getCurrency() != CurrencyCode.RSD && toCurrency != CurrencyCode.RSD) {
            BigDecimal convertedToRsd =
                exchangeRateService.convertCurrency(
                    amount,
                    fromAccount.getCurrency(),
                    CurrencyCode.RSD
                );
            convertedAmount =
                exchangeRateService.convertCurrency(convertedToRsd, CurrencyCode.RSD, toCurrency);
        } else {
            convertedAmount =
                exchangeRateService.convertCurrency(amount, fromAccount.getCurrency(), toCurrency);
        }
        createBankTransfer(
            fromAccount,
            fromBankAccount.getAccountNumber(),
            fromBankAccount.getCurrency(),
            amount,
            "Bank transfer",
            id
        );
        createBankTransfer(
            toBankAccount,
            toAccountNumber,
            toCurrency,
            convertedAmount,
            "Bank transfer",
            id
        );
    }

    public void createBankTransfer(
        Account fromAccount,
        String toAccountNumber,
        CurrencyCode toCurrency,
        BigDecimal amount,
        String purpose,
        ForeignBankId id
    ) {
        Transaction transaction =
            buildSpecialTransaction(
                fromAccount,
                toAccountNumber,
                toCurrency,
                amount,
                amount,
                BigDecimal.ZERO,
                "Bank Transfer",
                "290",
                "TRF-" + UUID.randomUUID(),
                purpose
            );
        transaction.setExecutingTransaction(id);
        transactionRepository.save(transaction);
    }

}
