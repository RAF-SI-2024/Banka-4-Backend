package rs.banka4.user_service.unit.transaction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.currency.db.Currency;
import rs.banka4.user_service.domain.transaction.db.Transaction;
import rs.banka4.user_service.domain.transaction.dtos.CreateTransactionDto;
import rs.banka4.user_service.generator.TransactionObjectMother;
import rs.banka4.user_service.repositories.AccountRepository;
import rs.banka4.user_service.repositories.TransactionRepository;
import rs.banka4.user_service.service.impl.BankAccountServiceImpl;
import rs.banka4.user_service.service.impl.ExchangeRateService;
import rs.banka4.user_service.service.impl.TransactionServiceImpl;

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
    void testProcessTransactionSameCurrency() {
        // Arrange
        Account fromAccount = new Account();
        fromAccount.setCurrency(TransactionObjectMother.generateCurrency("RSD"));
        fromAccount.setBalance(BigDecimal.valueOf(1000));

        Account toAccount = new Account();
        toAccount.setCurrency(TransactionObjectMother.generateCurrency("RSD"));
        toAccount.setBalance(BigDecimal.valueOf(500));

        BigDecimal amount = BigDecimal.valueOf(100);
        CreateTransactionDto createTransactionDto =
            TransactionObjectMother.generateBasicCreatePaymentDto();

        // Act
        Transaction transaction =
            transactionService.processTransaction(
                fromAccount,
                toAccount,
                amount,
                createTransactionDto
            );

        // Assert
        assertNotNull(transaction);
        assertEquals(BigDecimal.valueOf(900), fromAccount.getBalance());
        assertEquals(BigDecimal.valueOf(600), toAccount.getBalance());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testProcessTransactionRsdToForeign() {
        // Arrange
        Account fromAccount = new Account();
        fromAccount.setCurrency(TransactionObjectMother.generateCurrency("RSD"));
        fromAccount.setBalance(BigDecimal.valueOf(1000));

        Account toAccount = new Account();
        toAccount.setCurrency(TransactionObjectMother.generateCurrency("USD"));
        toAccount.setBalance(BigDecimal.valueOf(500));

        BigDecimal amount = BigDecimal.valueOf(100);
        CreateTransactionDto createTransactionDto =
            TransactionObjectMother.generateBasicCreatePaymentDto();

        when(exchangeRateService.calculateFee(amount)).thenReturn(BigDecimal.valueOf(10));
        when(exchangeRateService.convertCurrency(amount, Currency.Code.RSD, Currency.Code.USD))
            .thenReturn(BigDecimal.valueOf(1));

        Account rsdBankAccount = new Account();
        rsdBankAccount.setCurrency(TransactionObjectMother.generateCurrency("RSD"));
        rsdBankAccount.setBalance(BigDecimal.valueOf(1000));

        Account usdBankAccount = new Account();
        usdBankAccount.setCurrency(TransactionObjectMother.generateCurrency("USD"));
        usdBankAccount.setBalance(BigDecimal.valueOf(1000));

        when(bankAccountServiceImpl.getBankAccountForCurrency(Currency.Code.RSD)).thenReturn(
            rsdBankAccount
        );
        when(bankAccountServiceImpl.getBankAccountForCurrency(Currency.Code.USD)).thenReturn(
            usdBankAccount
        );

        // Act
        Transaction transaction =
            transactionService.processTransaction(
                fromAccount,
                toAccount,
                amount,
                createTransactionDto
            );

        // Assert
        assertNotNull(transaction);
        assertEquals(BigDecimal.valueOf(890), fromAccount.getBalance());
        assertEquals(BigDecimal.valueOf(501), toAccount.getBalance());
        verify(transactionRepository, times(5)).save(any(Transaction.class));
    }

    @Test
    void testProcessTransactionForeignToRsd() {
        // Arrange
        Account fromAccount = new Account();
        fromAccount.setCurrency(TransactionObjectMother.generateCurrency("USD"));
        fromAccount.setBalance(BigDecimal.valueOf(1000));

        Account toAccount = new Account();
        toAccount.setCurrency(TransactionObjectMother.generateCurrency("RSD"));
        toAccount.setBalance(BigDecimal.valueOf(500));

        BigDecimal amount = BigDecimal.valueOf(100);
        CreateTransactionDto createTransactionDto =
            TransactionObjectMother.generateBasicCreatePaymentDto();

        when(exchangeRateService.calculateFee(amount)).thenReturn(BigDecimal.valueOf(10));
        when(exchangeRateService.convertCurrency(amount, Currency.Code.USD, Currency.Code.RSD))
            .thenReturn(BigDecimal.valueOf(1000));

        Account usdBankAccount = new Account();
        usdBankAccount.setCurrency(TransactionObjectMother.generateCurrency("USD"));
        usdBankAccount.setBalance(BigDecimal.valueOf(1000));

        Account rsdBankAccount = new Account();
        rsdBankAccount.setCurrency(TransactionObjectMother.generateCurrency("RSD"));
        rsdBankAccount.setBalance(BigDecimal.valueOf(1000));

        when(bankAccountServiceImpl.getBankAccountForCurrency(Currency.Code.USD)).thenReturn(
            usdBankAccount
        );
        when(bankAccountServiceImpl.getBankAccountForCurrency(Currency.Code.RSD)).thenReturn(
            rsdBankAccount
        );

        // Act
        Transaction transaction =
            transactionService.processTransaction(
                fromAccount,
                toAccount,
                amount,
                createTransactionDto
            );

        // Assert
        assertNotNull(transaction);
        assertEquals(BigDecimal.valueOf(890), fromAccount.getBalance());
        assertEquals(BigDecimal.valueOf(1500), toAccount.getBalance());
        verify(transactionRepository, times(5)).save(any(Transaction.class));
    }

    @Test
    void testProcessTransactionForeignToForeign() {
        // Arrange
        Account fromAccount = new Account();
        fromAccount.setCurrency(TransactionObjectMother.generateCurrency("EUR"));
        fromAccount.setBalance(BigDecimal.valueOf(1000));

        Account toAccount = new Account();
        toAccount.setCurrency(TransactionObjectMother.generateCurrency("USD"));
        toAccount.setBalance(BigDecimal.valueOf(500));

        BigDecimal amount = BigDecimal.valueOf(100);
        CreateTransactionDto createTransactionDto =
            TransactionObjectMother.generateBasicCreatePaymentDto();

        when(exchangeRateService.calculateFee(amount)).thenReturn(BigDecimal.valueOf(10));
        when(exchangeRateService.convertCurrency(amount, Currency.Code.EUR, Currency.Code.RSD))
            .thenReturn(BigDecimal.valueOf(1000));
        when(
            exchangeRateService.convertCurrency(
                BigDecimal.valueOf(1000),
                Currency.Code.RSD,
                Currency.Code.USD
            )
        ).thenReturn(BigDecimal.valueOf(1));

        Account eurBankAccount = new Account();
        eurBankAccount.setCurrency(TransactionObjectMother.generateCurrency("EUR"));
        eurBankAccount.setBalance(BigDecimal.valueOf(1000));

        Account rsdBankAccount = new Account();
        rsdBankAccount.setCurrency(TransactionObjectMother.generateCurrency("RSD"));
        rsdBankAccount.setBalance(BigDecimal.valueOf(1000));

        Account usdBankAccount = new Account();
        usdBankAccount.setCurrency(TransactionObjectMother.generateCurrency("USD"));
        usdBankAccount.setBalance(BigDecimal.valueOf(1000));

        when(bankAccountServiceImpl.getBankAccountForCurrency(Currency.Code.EUR)).thenReturn(
            eurBankAccount
        );
        when(bankAccountServiceImpl.getBankAccountForCurrency(Currency.Code.RSD)).thenReturn(
            rsdBankAccount
        );
        when(bankAccountServiceImpl.getBankAccountForCurrency(Currency.Code.USD)).thenReturn(
            usdBankAccount
        );

        // Act
        Transaction transaction =
            transactionService.processTransaction(
                fromAccount,
                toAccount,
                amount,
                createTransactionDto
            );

        // Assert
        assertNotNull(transaction);
        assertEquals(BigDecimal.valueOf(880), fromAccount.getBalance());
        assertEquals(BigDecimal.valueOf(501), toAccount.getBalance());
        verify(transactionRepository, times(8)).save(any(Transaction.class));
    }
}
