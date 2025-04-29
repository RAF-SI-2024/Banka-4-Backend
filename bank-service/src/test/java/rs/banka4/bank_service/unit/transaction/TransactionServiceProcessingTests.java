package rs.banka4.bank_service.unit.transaction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import rs.banka4.bank_service.domain.account.db.Account;
import rs.banka4.bank_service.domain.transaction.db.Transaction;
import rs.banka4.bank_service.domain.transaction.dtos.CreateTransactionDto;
import rs.banka4.bank_service.generator.TransactionObjectMother;
import rs.banka4.bank_service.repositories.AccountRepository;
import rs.banka4.bank_service.repositories.TransactionRepository;
import rs.banka4.bank_service.service.abstraction.ExchangeRateService;
import rs.banka4.bank_service.service.impl.BankAccountServiceImpl;
import rs.banka4.bank_service.service.impl.TransactionServiceImpl;
import rs.banka4.rafeisen.common.currency.CurrencyCode;

public class TransactionServiceProcessingTests {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private ExchangeRateService exchangeRateService;
    @Mock
    private BankAccountServiceImpl bankAccountServiceImpl;
    @InjectMocks
    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @Disabled
    void testProcessTransactionSameCurrency() {
        // Arrange
        Account fromAccount = new Account();
        fromAccount.setCurrency(TransactionObjectMother.generateCurrency(CurrencyCode.RSD));
        fromAccount.setBalance(BigDecimal.valueOf(1000));

        Account toAccount = new Account();
        toAccount.setCurrency(TransactionObjectMother.generateCurrency(CurrencyCode.RSD));
        toAccount.setBalance(BigDecimal.valueOf(500));

        BigDecimal amount = BigDecimal.valueOf(100);
        CreateTransactionDto createTransactionDto =
            TransactionObjectMother.generateBasicCreatePaymentDto();

        // Act
        Transaction transaction =
            transactionService.processTransaction(
                fromAccount,
                toAccount.getAccountNumber(),
                toAccount.getCurrency(),
                createTransactionDto
            );

        // Assert
        assertNotNull(transaction);
        assertEquals(BigDecimal.valueOf(900), fromAccount.getBalance());
        assertEquals(BigDecimal.valueOf(600), toAccount.getBalance());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @Disabled
    void testProcessTransactionRsdToForeign() {
        // Arrange
        Account fromAccount = new Account();
        fromAccount.setCurrency(TransactionObjectMother.generateCurrency(CurrencyCode.RSD));
        fromAccount.setBalance(BigDecimal.valueOf(1000));

        Account toAccount = new Account();
        toAccount.setCurrency(TransactionObjectMother.generateCurrency(CurrencyCode.USD));
        toAccount.setBalance(BigDecimal.valueOf(500));

        BigDecimal amount = BigDecimal.valueOf(100);
        CreateTransactionDto createTransactionDto =
            TransactionObjectMother.generateBasicCreatePaymentDto();

        when(exchangeRateService.calculateFee(amount)).thenReturn(BigDecimal.valueOf(10));
        when(exchangeRateService.convertCurrency(amount, CurrencyCode.RSD, CurrencyCode.USD))
            .thenReturn(BigDecimal.valueOf(1));

        Account rsdBankAccount = new Account();
        rsdBankAccount.setCurrency(TransactionObjectMother.generateCurrency(CurrencyCode.RSD));
        rsdBankAccount.setBalance(BigDecimal.valueOf(1000));

        Account usdBankAccount = new Account();
        usdBankAccount.setCurrency(TransactionObjectMother.generateCurrency(CurrencyCode.USD));
        usdBankAccount.setBalance(BigDecimal.valueOf(1000));

        when(bankAccountServiceImpl.getBankAccountForCurrency(CurrencyCode.RSD)).thenReturn(
            rsdBankAccount
        );
        when(bankAccountServiceImpl.getBankAccountForCurrency(CurrencyCode.USD)).thenReturn(
            usdBankAccount
        );

        // Act
        Transaction transaction =
            transactionService.processTransaction(
                fromAccount,
                toAccount.getAccountNumber(),
                toAccount.getCurrency(),
                createTransactionDto
            );

        // Assert
        assertNotNull(transaction);
        assertEquals(BigDecimal.valueOf(890), fromAccount.getBalance());
        assertEquals(BigDecimal.valueOf(501), toAccount.getBalance());
        verify(transactionRepository, times(5)).save(any(Transaction.class));
    }

    @Test
    @Disabled
    void testProcessTransactionForeignToRsd() {
        // Arrange
        Account fromAccount = new Account();
        fromAccount.setCurrency(TransactionObjectMother.generateCurrency(CurrencyCode.USD));
        fromAccount.setBalance(BigDecimal.valueOf(1000));

        Account toAccount = new Account();
        toAccount.setCurrency(TransactionObjectMother.generateCurrency(CurrencyCode.RSD));
        toAccount.setBalance(BigDecimal.valueOf(500));

        BigDecimal amount = BigDecimal.valueOf(100);
        CreateTransactionDto createTransactionDto =
            TransactionObjectMother.generateBasicCreatePaymentDto();

        when(exchangeRateService.calculateFee(amount)).thenReturn(BigDecimal.valueOf(10));
        when(exchangeRateService.convertCurrency(amount, CurrencyCode.USD, CurrencyCode.RSD))
            .thenReturn(BigDecimal.valueOf(1000));

        Account usdBankAccount = new Account();
        usdBankAccount.setCurrency(TransactionObjectMother.generateCurrency(CurrencyCode.USD));
        usdBankAccount.setBalance(BigDecimal.valueOf(1000));

        Account rsdBankAccount = new Account();
        rsdBankAccount.setCurrency(TransactionObjectMother.generateCurrency(CurrencyCode.RSD));
        rsdBankAccount.setBalance(BigDecimal.valueOf(1000));

        when(bankAccountServiceImpl.getBankAccountForCurrency(CurrencyCode.USD)).thenReturn(
            usdBankAccount
        );
        when(bankAccountServiceImpl.getBankAccountForCurrency(CurrencyCode.RSD)).thenReturn(
            rsdBankAccount
        );

        // Act
        Transaction transaction =
            transactionService.processTransaction(
                fromAccount,
                toAccount.getAccountNumber(),
                toAccount.getCurrency(),
                createTransactionDto
            );

        // Assert
        assertNotNull(transaction);
        assertEquals(BigDecimal.valueOf(890), fromAccount.getBalance());
        assertEquals(BigDecimal.valueOf(1500), toAccount.getBalance());
        verify(transactionRepository, times(5)).save(any(Transaction.class));
    }

    @Test
    @Disabled
    void testProcessTransactionForeignToForeign() {
        // Arrange
        Account fromAccount = new Account();
        fromAccount.setCurrency(TransactionObjectMother.generateCurrency(CurrencyCode.EUR));
        fromAccount.setBalance(BigDecimal.valueOf(1000));

        Account toAccount = new Account();
        toAccount.setCurrency(TransactionObjectMother.generateCurrency(CurrencyCode.USD));
        toAccount.setBalance(BigDecimal.valueOf(500));

        BigDecimal amount = BigDecimal.valueOf(100);
        CreateTransactionDto createTransactionDto =
            TransactionObjectMother.generateBasicCreatePaymentDto();

        when(exchangeRateService.calculateFee(amount)).thenReturn(BigDecimal.valueOf(10));
        when(exchangeRateService.convertCurrency(amount, CurrencyCode.EUR, CurrencyCode.RSD))
            .thenReturn(BigDecimal.valueOf(1000));
        when(
            exchangeRateService.convertCurrency(
                BigDecimal.valueOf(1000),
                CurrencyCode.RSD,
                CurrencyCode.USD
            )
        ).thenReturn(BigDecimal.valueOf(1));

        Account eurBankAccount = new Account();
        eurBankAccount.setCurrency(TransactionObjectMother.generateCurrency(CurrencyCode.EUR));
        eurBankAccount.setBalance(BigDecimal.valueOf(1000));

        Account rsdBankAccount = new Account();
        rsdBankAccount.setCurrency(TransactionObjectMother.generateCurrency(CurrencyCode.RSD));
        rsdBankAccount.setBalance(BigDecimal.valueOf(1000));

        Account usdBankAccount = new Account();
        usdBankAccount.setCurrency(TransactionObjectMother.generateCurrency(CurrencyCode.USD));
        usdBankAccount.setBalance(BigDecimal.valueOf(1000));

        when(bankAccountServiceImpl.getBankAccountForCurrency(CurrencyCode.EUR)).thenReturn(
            eurBankAccount
        );
        when(bankAccountServiceImpl.getBankAccountForCurrency(CurrencyCode.RSD)).thenReturn(
            rsdBankAccount
        );
        when(bankAccountServiceImpl.getBankAccountForCurrency(CurrencyCode.USD)).thenReturn(
            usdBankAccount
        );

        // Act
        Transaction transaction =
            transactionService.processTransaction(
                fromAccount,
                toAccount.getAccountNumber(),
                toAccount.getCurrency(),
                createTransactionDto
            );

        // Assert
        assertNotNull(transaction);
        assertEquals(BigDecimal.valueOf(880), fromAccount.getBalance());
        assertEquals(BigDecimal.valueOf(501), toAccount.getBalance());
        verify(transactionRepository, times(8)).save(any(Transaction.class));
    }
}
