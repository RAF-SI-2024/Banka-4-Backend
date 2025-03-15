package rs.banka4.user_service.unit.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.currency.db.Currency;
import rs.banka4.user_service.domain.transaction.dtos.TransactionDto;
import rs.banka4.user_service.domain.transaction.db.Transaction;
import rs.banka4.user_service.domain.transaction.db.TransactionStatus;
import rs.banka4.user_service.domain.transaction.mapper.TransactionMapper;
import rs.banka4.user_service.exceptions.transaction.TransactionNotFound;
import rs.banka4.user_service.generator.AccountObjectMother;
import rs.banka4.user_service.generator.TransactionObjectMother;
import rs.banka4.user_service.repositories.AccountRepository;
import rs.banka4.user_service.repositories.TransactionRepository;
import rs.banka4.user_service.service.impl.TransactionServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TransactionServiceGetTests {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionMapper transactionMapper;
    @InjectMocks
    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private static Stream<Arguments> provideFilters() {
        return Stream.of(
                Arguments.of(TransactionStatus.REALIZED, BigDecimal.ONE, LocalDate.now(), "1265463698391"),
                Arguments.of(TransactionStatus.IN_PROGRESS, BigDecimal.TEN, LocalDate.now().minusDays(1), "1265463698392"),
                Arguments.of(null, null, null, null)
        );
    }

    private static Stream<Arguments> provideAdvancedFilters() {
        return Stream.of(
                Arguments.of(TransactionStatus.REALIZED, BigDecimal.ONE, null, null, 1),
                Arguments.of(TransactionStatus.IN_PROGRESS, BigDecimal.TEN, LocalDate.now().minusDays(1), "1261463698392", 0),
                Arguments.of(null, null, null, null, 3)
        );
    }

    @ParameterizedTest
    @MethodSource("provideFilters")
    void testGetAllTransactionsForClient(TransactionStatus status, BigDecimal amount, LocalDate date, String accountNumber) {
        // Arrange
        String token = "mocked-token";
        Account fromAccount = AccountObjectMother.generateBasicFromAccount();
        TransactionDto transactionDto = TransactionObjectMother.generateBasicTransactionDto();
        Transaction transaction = TransactionObjectMother.generateBasicTransaction(
                fromAccount,
                AccountObjectMother.generateBasicToAccount()
        );
        Page<Transaction> transactions = new PageImpl<>(Collections.singletonList(transaction));
        PageRequest pageRequest = PageRequest.of(0, 10);

        when(transactionRepository.findAll((Specification<Transaction>) any(), eq(pageRequest))).thenReturn(transactions);
        when(transactionMapper.toDto(transaction)).thenReturn(transactionDto);
        when(accountRepository.findAccountByAccountNumber(accountNumber)).thenReturn(Optional.of(fromAccount));

        // Act
        Page<TransactionDto> result = transactionService.getAllTransactionsForClient(token, status, amount, date, accountNumber, pageRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(transactionDto, result.getContent().getFirst());
        assertEquals(transactionDto.transactionNumber(), result.getContent().getFirst().transactionNumber());
        assertEquals(transactionDto.fromAccount(), result.getContent().getFirst().fromAccount());
        assertEquals(transactionDto.toAccount(), result.getContent().getFirst().toAccount());
        assertEquals(transactionDto.fromAccount(), result.getContent().getFirst().fromAccount());
        assertEquals(transactionDto.recipient(), result.getContent().getFirst().recipient());
        assertEquals(transactionDto.paymentCode(), result.getContent().getFirst().paymentCode());
        assertEquals(transactionDto.referenceNumber(), result.getContent().getFirst().referenceNumber());
        assertEquals(transactionDto.paymentPurpose(), result.getContent().getFirst().paymentPurpose());
        assertEquals(transactionDto.paymentDateTime(), result.getContent().getFirst().paymentDateTime());
        assertEquals(transactionDto.status(), result.getContent().getFirst().status());
    }

    // Using if statements in tests are bad practice, but must be used in this case
    @ParameterizedTest
    @MethodSource("provideAdvancedFilters")
    void testGetAllTransactionsForClient(TransactionStatus status, BigDecimal amount, LocalDate date, String accountNumber, int expectedSize) {
        // Arrange
        String token = "mocked-token";
        Account fromAccount = AccountObjectMother.generateBasicFromAccount();
        Account toAccount = AccountObjectMother.generateBasicToAccount();
        Currency currency = TransactionObjectMother.generateCurrency("EUR");

        Transaction transaction1 = TransactionObjectMother.generateTransaction(UUID.randomUUID(), fromAccount, toAccount, BigDecimal.ONE, currency, TransactionStatus.REALIZED);
        Transaction transaction2 = TransactionObjectMother.generateTransaction(UUID.randomUUID(), fromAccount, toAccount, BigDecimal.TEN, currency, TransactionStatus.IN_PROGRESS);
        Transaction transaction3 = TransactionObjectMother.generateTransaction(UUID.randomUUID(), fromAccount, toAccount, BigDecimal.valueOf(5), currency, TransactionStatus.REALIZED);

        PageRequest pageRequest = PageRequest.of(0, 10);

        when(accountRepository.findAccountByAccountNumber(accountNumber)).thenReturn(Optional.of(fromAccount));

        ArgumentCaptor<Specification<Transaction>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        when(transactionRepository.findAll(specCaptor.capture(), eq(pageRequest))).thenAnswer(invocation -> {
            List<Transaction> filteredTransactions = Stream.of(transaction1, transaction2, transaction3)
                    .filter(transaction -> {
                        if (status != null && !transaction.getStatus().equals(status)) return false;
                        if (amount != null && transaction.getFrom().getAmount().compareTo(amount) != 0) return false;
                        if (date != null && !transaction.getPaymentDateTime().toLocalDate().equals(date)) return false;
                        if (accountNumber != null && !transaction.getFromAccount().getAccountNumber().equals(accountNumber) && !transaction.getToAccount().getAccountNumber().equals(accountNumber)) return false;
                        return true;
                    })
                    .collect(Collectors.toList());
            return new PageImpl<>(filteredTransactions, pageRequest, filteredTransactions.size());
        });

        when(transactionMapper.toDto(transaction1)).thenReturn(TransactionObjectMother.generateTransactionDto(transaction1.getId(), fromAccount.getAccountNumber(), toAccount.getAccountNumber(), BigDecimal.ONE, "EUR", TransactionStatus.REALIZED));
        when(transactionMapper.toDto(transaction2)).thenReturn(TransactionObjectMother.generateTransactionDto(transaction2.getId(), fromAccount.getAccountNumber(), toAccount.getAccountNumber(), BigDecimal.TEN, "EUR", TransactionStatus.IN_PROGRESS));
        when(transactionMapper.toDto(transaction3)).thenReturn(TransactionObjectMother.generateTransactionDto(transaction3.getId(), fromAccount.getAccountNumber(), toAccount.getAccountNumber(), BigDecimal.valueOf(5), "EUR", TransactionStatus.REALIZED));

        // Act
        Page<TransactionDto> result = transactionService.getAllTransactionsForClient(token, status, amount, date, accountNumber, pageRequest);

        // Assert
        assertNotNull(result);
        assertEquals(expectedSize, result.getTotalElements());
    }

    @Test
    void testGetTransactionById() {
        // Arrange
        String token = "mocked-token";
        UUID transactionId = UUID.randomUUID();
        TransactionDto transactionDto = TransactionObjectMother.generateBasicTransactionDto();
        Transaction transaction = TransactionObjectMother.generateBasicTransaction(
                AccountObjectMother.generateBasicFromAccount(),
                AccountObjectMother.generateBasicToAccount()
        );

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(transactionMapper.toDto(transaction)).thenReturn(transactionDto);

        // Act
        TransactionDto result = transactionService.getTransactionById(token, transactionId);

        // Assert
        assertNotNull(result);
        assertEquals(transactionDto, result);
    }

    @Test
    void testGetTransactionByIdNotFound() {
        // Arrange
        String token = "mocked-token";
        UUID transactionId = UUID.randomUUID();

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TransactionNotFound.class, () -> transactionService.getTransactionById(token, transactionId));
    }
}