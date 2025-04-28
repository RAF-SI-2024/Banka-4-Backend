package rs.banka4.bank_service.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
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
    private final EntityManager entityManager;
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

        Map<String, Account> lockedAccounts =
            lockAccounts(createPaymentDto.fromAccount(), createPaymentDto.toAccount());

        Account fromAccount = lockedAccounts.get(createPaymentDto.fromAccount());
        Account toAccount = lockedAccounts.get(createPaymentDto.toAccount());

        if (
            fromAccount.getClient()
                .equals(toAccount.getClient())
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

        Transaction transaction = processTransaction(fromAccount, toAccount, createPaymentDto);

        if (createPaymentDto.saveRecipient()) {
            ClientContact clientContact =
                ClientContact.builder()
                    .client(client)
                    .accountNumber(toAccount.getAccountNumber())
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

        Map<String, Account> lockedAccounts =
            lockAccounts(createTransferDto.fromAccount(), createTransferDto.toAccount());

        Account fromAccount = lockedAccounts.get(createTransferDto.fromAccount());
        Account toAccount = lockedAccounts.get(createTransferDto.toAccount());

        if (fromAccount.equals(toAccount)) throw new ClientCannotTransferToSameAccount();

        validateAccountActive(fromAccount);
        validateClientAccountOwnership(client, fromAccount, toAccount);
        validateSufficientFunds(fromAccount, createTransferDto.fromAmount());

        Transaction transaction = processTransaction(fromAccount, toAccount, createTransferDto);

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

    /**
     * Processes a transaction between two accounts.
     * <p>
     * Handles the transfer of funds between the specified accounts, including any necessary
     * currency conversions and fee calculations. It supports transactions between accounts with the
     * same currency, as well as transactions involving different currencies.
     * </p>
     *
     * @param fromAccount the account from which the funds will be debited
     * @param toAccount the account to which the funds will be credited
     * @param amount the amount to be transferred
     * @param createTransactionDto the data transfer object containing transaction details
     * @return the processed transaction
     */
    @Transactional
    public Transaction processTransaction(
        Account fromAccount,
        Account toAccount,
        BigDecimal amount,
        CreateTransactionDto createTransactionDto
    ) {
        BigDecimal fee = BigDecimal.ZERO;
        // Same -> Same
        if (
            fromAccount.getCurrency()
                .equals(toAccount.getCurrency())
        ) {
            transferAmount(fromAccount, toAccount, amount);
        }
        // RSD -> Foreign
        else
            if (
                fromAccount.getCurrency()
                    .equals(CurrencyCode.RSD)
            ) {
                fee = transferFromRsdToForeign(fromAccount, toAccount, amount);
            } else {
                // Foreign -> RSD
                if (
                    toAccount.getCurrency()
                        .equals(CurrencyCode.RSD)
                ) {
                    fee = transferFromForeignToRsd(fromAccount, toAccount, amount);
                }
                // Foreign -> Foreign
                else {
                    fee = transferFromForeignToForeign(fromAccount, toAccount, amount);
                }
            }

        Transaction transaction;
        if (createTransactionDto instanceof CreatePaymentDto) {
            transaction =
                buildTransaction(
                    fromAccount,
                    toAccount,
                    (CreatePaymentDto) createTransactionDto,
                    fee,
                    TransactionStatus.REALIZED
                );
        } else {
            transaction =
                buildTransfer(
                    fromAccount,
                    toAccount,
                    (CreateTransferDto) createTransactionDto,
                    fee,
                    TransactionStatus.REALIZED
                );
            transaction.setTransfer(true);
        }
        transactionRepository.save(transaction);
        return transaction;
    }

    /**
     * Transfers funds from an RSD account to a foreign currency account.
     * <p>
     * Handles the transfer of funds from an RSD account to a foreign currency account, including
     * fee calculation, currency conversion, and updating the balances of the involved accounts. It
     * ensures that the account has sufficient funds to cover the transfer amount and the associated
     * fee.
     * </p>
     *
     * @param fromAccount the RSD account from which the funds will be debited
     * @param toAccount the foreign currency account to which the funds will be credited
     * @param amount the amount to be transferred
     * @return the calculated fee for the transfer
     * @throws InsufficientFunds if the account has insufficient funds to cover the transfer amount
     *         and fee
     */
    @Transactional
    protected BigDecimal transferFromRsdToForeign(
        Account fromAccount,
        Account toAccount,
        BigDecimal amount
    ) {
        BigDecimal fee = exchangeRateService.calculateFee(amount);

        if (!hasEnoughFunds(fromAccount, fee.add(amount))) {
            throw new InsufficientFunds();
        }

        // Transfer Client RSD to bank's RSD account
        Account rsdBankAccount = bankAccountServiceImpl.getBankAccountForCurrency(CurrencyCode.RSD);

        transferAmount(fromAccount, rsdBankAccount, amount);

        createBankTransferTransaction(
            fromAccount,
            rsdBankAccount,
            amount,
            "Transfer to RSD Bank Account"
        );

        // Convert whole amount to foreign currency using sell rate
        BigDecimal convertedAmount =
            exchangeRateService.convertCurrency(
                amount,
                fromAccount.getCurrency(),
                toAccount.getCurrency()
            );

        // Charge the fee
        transferAmount(fromAccount, rsdBankAccount, fee);

        createFeeTransaction(fromAccount, rsdBankAccount, fee);

        // Decrease the bank account in foreign currency for the full amount that the user receives
        Account foreignBankAccount =
            bankAccountServiceImpl.getBankAccountForCurrency(toAccount.getCurrency());

        transferAmount(foreignBankAccount, toAccount, convertedAmount);

        createBankTransferTransaction(
            foreignBankAccount,
            toAccount,
            convertedAmount,
            "Transfer converted amount to Client"
        );

        // Transaction for fee & conversion
        createConversionTransaction(rsdBankAccount, foreignBankAccount, convertedAmount);

        return fee;
    }

    /**
     * Transfers funds from a foreign currency account to an RSD account.
     * <p>
     * Handles the transfer of funds from a foreign currency account to an RSD account, including
     * fee calculation, currency conversion, and updating the balances of the involved accounts. It
     * ensures that the account has sufficient funds to cover the transfer amount and the associated
     * fee.
     * </p>
     *
     * @param fromAccount the foreign currency account from which the funds will be debited
     * @param toAccount the RSD account to which the funds will be credited
     * @param amount the amount to be transferred
     * @return the calculated fee for the transfer
     * @throws InsufficientFunds if the account has insufficient funds to cover the transfer amount
     *         and fee
     */
    @Transactional
    protected BigDecimal transferFromForeignToRsd(
        Account fromAccount,
        Account toAccount,
        BigDecimal amount
    ) {
        BigDecimal fee = exchangeRateService.calculateFee(amount);

        if (!hasEnoughFunds(fromAccount, fee.add(amount))) {
            throw new InsufficientFunds();
        }

        // Transfer Foreign from client to Foreign bank account
        Account foreignBankAccount =
            bankAccountServiceImpl.getBankAccountForCurrency(fromAccount.getCurrency());

        transferAmount(fromAccount, foreignBankAccount, amount);

        createBankTransferTransaction(
            fromAccount,
            foreignBankAccount,
            amount,
            "Transfer to Foreign Bank Account"
        );

        // Convert Foreign to RSD using the buy rate
        BigDecimal convertedAmount =
            exchangeRateService.convertCurrency(
                amount,
                fromAccount.getCurrency(),
                CurrencyCode.RSD
            );

        // Transfer RSD from RSD bank account to client
        Account rsdBankAccount = bankAccountServiceImpl.getBankAccountForCurrency(CurrencyCode.RSD);

        transferAmount(rsdBankAccount, toAccount, convertedAmount);

        createBankTransferTransaction(
            rsdBankAccount,
            toAccount,
            convertedAmount,
            "Transfer converted amount to Client"
        );

        // Charge the fee
        transferAmount(fromAccount, foreignBankAccount, fee);

        createFeeTransaction(fromAccount, foreignBankAccount, fee);

        // Transaction for conversion
        createConversionTransaction(foreignBankAccount, rsdBankAccount, convertedAmount);

        return fee;
    }

    /**
     * Transfers funds from one foreign currency account to another foreign currency account.
     * <p>
     * Handles the transfer of funds between two foreign currency accounts, including fee
     * calculation, currency conversion, and updating the balances of the involved accounts. It
     * ensures that the account has sufficient funds to cover the transfer amount and the associated
     * fee.
     * </p>
     *
     * @param fromAccount the foreign currency account from which the funds will be debited
     * @param toAccount the foreign currency account to which the funds will be credited
     * @param amount the amount to be transferred
     * @return the calculated fee for the transfer
     * @throws InsufficientFunds if the account has insufficient funds to cover the transfer amount
     *         and fee
     */
    @Transactional
    protected BigDecimal transferFromForeignToForeign(
        Account fromAccount,
        Account toAccount,
        BigDecimal amount
    ) {
        BigDecimal fee =
            exchangeRateService.calculateFee(amount)
                .multiply(BigDecimal.TWO);

        if (!hasEnoughFunds(fromAccount, fee.add(amount))) {
            throw new InsufficientFunds();
        }

        // Transfer ForeignFrom from client to ForeignFrom bank account (EUR Client -> EUR Bank)
        Account foreignBankAccount =
            bankAccountServiceImpl.getBankAccountForCurrency(fromAccount.getCurrency());

        transferAmount(fromAccount, foreignBankAccount, amount);

        createBankTransferTransaction(
            fromAccount,
            foreignBankAccount,
            amount,
            "Transfer to Foreign Bank Account"
        );

        // Convert ForeignFrom to RSD using the sell rate EUR Bank -> RSD Bank
        Account rsdBankAccount = bankAccountServiceImpl.getBankAccountForCurrency(CurrencyCode.RSD);
        BigDecimal amountInRSD =
            exchangeRateService.convertCurrency(
                amount,
                fromAccount.getCurrency(),
                CurrencyCode.RSD
            );

        transferAmountDifferentCurrencies(foreignBankAccount, rsdBankAccount, amount, amountInRSD);

        createBankTransferTransaction(
            foreignBankAccount,
            rsdBankAccount,
            amount,
            "Transfer to RSD Bank Account"
        );

        // Fee
        transferAmount(fromAccount, foreignBankAccount, fee);

        createFeeTransaction(fromAccount, foreignBankAccount, fee);

        BigDecimal amountInForeignTo =
            exchangeRateService.convertCurrency(
                amountInRSD,
                CurrencyCode.RSD,
                toAccount.getCurrency()
            );
        Account foreignToBankAccount =
            bankAccountServiceImpl.getBankAccountForCurrency(CurrencyCode.USD);

        // Transfer RSD from RSD bank account to ForeignTo bank account (RSD Bank -> USD Bank)
        transferAmountDifferentCurrencies(
            rsdBankAccount,
            foreignToBankAccount,
            amountInRSD,
            amountInForeignTo
        );

        // Transfer ForeignTo from ForeignTo bank account to client (USD Bank -> USD Client)

        transferAmount(foreignToBankAccount, toAccount, amountInForeignTo);

        createBankTransferTransaction(
            foreignToBankAccount,
            toAccount,
            amountInForeignTo,
            "Transfer converted amount to Client"
        );

        // Create a transaction for the conversion
        createConversionTransaction(foreignBankAccount, rsdBankAccount, amountInRSD);
        createConversionTransaction(rsdBankAccount, foreignToBankAccount, amountInRSD);
        createConversionTransaction(foreignToBankAccount, toAccount, amountInForeignTo);

        return fee;
    }

    // Private methods
    private Transaction buildTransaction(
        Account fromAccount,
        Account toAccount,
        CreatePaymentDto createPaymentDto,
        BigDecimal fee,
        TransactionStatus status
    ) {
        BigDecimal toAmount =
            convertCurrency(
                createPaymentDto.fromAmount(),
                fromAccount.getCurrency(),
                toAccount.getCurrency()
            );

        return Transaction.builder()
            .transactionNumber(
                UUID.randomUUID()
                    .toString()
            )
            .fromAccount(fromAccount.getAccountNumber())
            .toAccount(toAccount.getAccountNumber())
            .from(new MonetaryAmount(createPaymentDto.fromAmount(), fromAccount.getCurrency()))
            .to(new MonetaryAmount(toAmount, toAccount.getCurrency()))
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
        Account toAccount,
        CreateTransferDto createTransferDto,
        BigDecimal fee,
        TransactionStatus status
    ) {
        BigDecimal toAmount =
            convertCurrency(
                createTransferDto.fromAmount(),
                fromAccount.getCurrency(),
                toAccount.getCurrency()
            );

        return Transaction.builder()
            .transactionNumber(
                UUID.randomUUID()
                    .toString()
            )
            .fromAccount(fromAccount.getAccountNumber())
            .toAccount(toAccount.getAccountNumber())
            .from(new MonetaryAmount(createTransferDto.fromAmount(), fromAccount.getCurrency()))
            .to(new MonetaryAmount(toAmount, toAccount.getCurrency()))
            .fee(new MonetaryAmount(fee, fromAccount.getCurrency()))
            .recipient(toAccount.getClient().firstName)
            .paymentCode("101")
            .referenceNumber(
                String.valueOf(
                    toAccount.getClient()
                        .getId()
                )
            )
            .paymentPurpose("Internal")
            .paymentDateTime(LocalDateTime.now())
            .status(status)
            .build();
    }


    private void createFeeTransaction(Account fromAccount, Account toAccount, BigDecimal fee) {
        BigDecimal toAmount =
            convertCurrency(fee, fromAccount.getCurrency(), toAccount.getCurrency());

        Transaction feeTransaction =
            buildSpecialTransaction(
                fromAccount,
                toAccount,
                fee,
                toAmount,
                fee,
                "Bank Fee",
                "289",
                "FEE-" + UUID.randomUUID(),
                "Transaction Fee"
            );
        transactionRepository.save(feeTransaction);
    }

    private void createConversionTransaction(
        Account fromAccount,
        Account toAccount,
        BigDecimal amount
    ) {
        BigDecimal toAmount =
            convertCurrency(amount, fromAccount.getCurrency(), toAccount.getCurrency());

        Transaction conversionTransaction =
            buildSpecialTransaction(
                fromAccount,
                toAccount,
                amount,
                toAmount,
                BigDecimal.ZERO,
                "Bank Conversion",
                "285",
                "CONV-" + UUID.randomUUID(),
                "Currency Conversion"
            );
        transactionRepository.save(conversionTransaction);
    }

    public void createBankTransferTransaction(
        Account fromAccount,
        Account toAccount,
        BigDecimal amount,
        String purpose
    ) {
        BigDecimal toAmount =
            convertCurrency(amount, fromAccount.getCurrency(), toAccount.getCurrency());

        Transaction transaction =
            buildSpecialTransaction(
                fromAccount,
                toAccount,
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
        Account toAccount,
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
            .toAccount(toAccount.getAccountNumber())
            .from(new MonetaryAmount(fromAmount, fromAccount.getCurrency()))
            .to(new MonetaryAmount(toAmount, toAccount.getCurrency()))
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
        return exchangeRateService.convertCurrency(amount, fromCurrency, toCurrency);
    }

    @Transactional
    protected Map<String, Account> lockAccounts(String... accountNumbers) {
        return Arrays.stream(accountNumbers)
            .distinct() // Remove duplicates
            .map(
                accNum -> accountRepository.findAccountByAccountNumber(accNum)
                    .orElseThrow(AccountNotFound::new)
            )
            .sorted(Comparator.comparing(Account::getId)) // Deadlock prevention
            .peek(account -> {
                entityManager.lock(account, LockModeType.PESSIMISTIC_WRITE);
                entityManager.refresh(account); // Refresh data
            })
            .collect(Collectors.toMap(Account::getAccountNumber, acc -> acc));
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
        Account toAccount,
        CreateTransactionDto createTransactionDto
    ) {
        BigDecimal fromAmount;
        String message;
        if (createTransactionDto instanceof CreatePaymentDto) {
            fromAmount = ((CreatePaymentDto) createTransactionDto).fromAmount();
            message = "Payment transaction";
        } else {
            fromAmount = ((CreateTransferDto) createTransactionDto).fromAmount();
            message = "Transfer transaction";
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
                toAccount.getCurrency(),
                new TxAccount.Account(toAccount.getAccountNumber())
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
                    toAccount,
                    (CreatePaymentDto) createTransactionDto,
                    fee,
                    TransactionStatus.IN_PROGRESS
                );
        } else {
            tx =
                buildTransfer(
                    fromAccount,
                    toAccount,
                    (CreateTransferDto) createTransactionDto,
                    fee,
                    TransactionStatus.IN_PROGRESS
                );
        }

        tx.setExecutingTransaction(id);
        transactionRepository.save(tx);

        return tx;
    }

}
