package rs.banka4.user_service.unit.transaction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.transaction.dtos.CreatePaymentDto;
import rs.banka4.user_service.domain.transaction.dtos.CreateTransferDto;
import rs.banka4.user_service.domain.transaction.dtos.TransactionDto;
import rs.banka4.user_service.domain.transaction.mapper.TransactionMapper;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.exceptions.account.AccountNotFound;
import rs.banka4.user_service.exceptions.account.NotAccountOwner;
import rs.banka4.user_service.exceptions.transaction.InsufficientFunds;
import rs.banka4.user_service.generator.AccountObjectMother;
import rs.banka4.user_service.generator.ClientObjectMother;
import rs.banka4.user_service.generator.TransactionObjectMother;
import rs.banka4.user_service.repositories.AccountRepository;
import rs.banka4.user_service.repositories.ClientContactRepository;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.repositories.TransactionRepository;
import rs.banka4.user_service.service.impl.TotpServiceImpl;
import rs.banka4.user_service.service.impl.TransactionServiceImpl;
import rs.banka4.user_service.utils.JwtUtil;

public class TransactionServiceCreateTests {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private Authentication authentication;
    @Mock
    private TransactionMapper transactionMapper;
    @Mock
    private TotpServiceImpl totpService;
    @InjectMocks
    private TransactionServiceImpl transactionService;
    @Mock
    private ClientContactRepository clientContactRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(authentication.getCredentials()).thenReturn("mocked-token");
        when(totpService.validate(anyString(), eq("123123"))).thenReturn(true);
    }

    @Test
    void testCreateTransactionSuccess() {
        // Arrange
        CreatePaymentDto createPaymentDto = TransactionObjectMother.generateBasicCreatePaymentDto();
        Client client =
            ClientObjectMother.generateClient(
                UUID.fromString("9df5e618-f21d-48a7-a7a4-ac55ea8bec97"),
                "markezaa@example.com"
            );
        Account fromAccount = AccountObjectMother.generateBasicFromAccount();
        Account toAccount = AccountObjectMother.generateBasicToAccount();
        TransactionDto transactionDto = TransactionObjectMother.generateBasicTransactionDto();

        client.setAccounts(Set.of(fromAccount));

        when(jwtUtil.extractUsername(anyString())).thenReturn("markezaa@example.com");
        when(clientRepository.findByEmail(anyString())).thenReturn(Optional.of(client));
        when(accountRepository.findAccountByAccountNumber(createPaymentDto.fromAccount()))
            .thenReturn(Optional.of(fromAccount));
        when(accountRepository.findAccountByAccountNumber(createPaymentDto.toAccount())).thenReturn(
            Optional.of(toAccount)
        );
        when(transactionMapper.toDto(any())).thenReturn(transactionDto);
        when(transactionRepository.getTotalDailyTransactions(any(), any())).thenReturn(
            BigDecimal.ZERO
        );
        when(transactionRepository.getTotalMonthlyTransactions(any(), anyInt())).thenReturn(
            BigDecimal.ZERO
        );

        // Act
        TransactionDto result =
            transactionService.createTransaction(authentication, createPaymentDto);

        // Assert
        assertNotNull(result);
        assertEquals(createPaymentDto.fromAccount(), result.fromAccount());
        assertEquals(createPaymentDto.toAccount(), result.toAccount());
        assertEquals(createPaymentDto.fromAmount(), result.fromAmount());
        assertEquals(createPaymentDto.recipient(), result.recipient());
        assertEquals(createPaymentDto.paymentCode(), result.paymentCode());
        assertEquals(createPaymentDto.referenceNumber(), result.referenceNumber());
        assertEquals(createPaymentDto.paymentPurpose(), result.paymentPurpose());
        verify(transactionRepository, times(1)).save(any());
    }

    @Test
    void testCreateTransactionInsufficientFunds() {
        // Arrange
        CreatePaymentDto createPaymentDto = TransactionObjectMother.generateBasicCreatePaymentDto();
        Client client =
            ClientObjectMother.generateClient(
                UUID.fromString("9df5e618-f21d-48a7-a7a4-ac55ea8bec97"),
                "markezaa@example.com"
            );
        Account fromAccount = AccountObjectMother.generateBasicFromAccount();
        Account toAccount = AccountObjectMother.generateBasicToAccount();

        fromAccount.setBalance(BigDecimal.valueOf(0.50)); // Insufficient funds
        client.setAccounts(Set.of(fromAccount));

        when(jwtUtil.extractUsername(anyString())).thenReturn("markezaa@example.com");
        when(clientRepository.findByEmail(anyString())).thenReturn(Optional.of(client));
        when(accountRepository.findAccountByAccountNumber(createPaymentDto.fromAccount()))
            .thenReturn(Optional.of(fromAccount));
        when(accountRepository.findAccountByAccountNumber(createPaymentDto.toAccount())).thenReturn(
            Optional.of(toAccount)
        );

        // Act & Assert
        assertThrows(
            InsufficientFunds.class,
            () -> transactionService.createTransaction(authentication, createPaymentDto)
        );
    }

    @Test
    void testCreateTransactionNotAccountOwner() {
        // Arrange
        CreatePaymentDto createPaymentDto = TransactionObjectMother.generateBasicCreatePaymentDto();
        Client client =
            ClientObjectMother.generateClient(
                UUID.fromString("9df5e618-f21d-48a7-a7a4-ac55ea8bec97"),
                "markezaa@example.com"
            );
        Account fromAccount = AccountObjectMother.generateBasicFromAccount();
        Account toAccount = AccountObjectMother.generateBasicToAccount();

        client.setAccounts(Set.of());

        when(jwtUtil.extractUsername(anyString())).thenReturn("markezaa@example.com");
        when(clientRepository.findByEmail(anyString())).thenReturn(Optional.of(client));
        when(accountRepository.findAccountByAccountNumber(createPaymentDto.fromAccount()))
            .thenReturn(Optional.of(fromAccount));
        when(accountRepository.findAccountByAccountNumber(createPaymentDto.toAccount())).thenReturn(
            Optional.of(toAccount)
        );

        // Act & Assert
        assertThrows(
            NotAccountOwner.class,
            () -> transactionService.createTransaction(authentication, createPaymentDto)
        );
    }

    @Test
    void testCreateTransactionToAccountNotFound() {
        // Arrange
        CreatePaymentDto createPaymentDto = TransactionObjectMother.generateBasicCreatePaymentDto();
        Client client =
            ClientObjectMother.generateClient(
                UUID.fromString("9df5e618-f21d-48a7-a7a4-ac55ea8bec97"),
                "markezaa@example.com"
            );
        Account fromAccount = AccountObjectMother.generateBasicFromAccount();

        client.setAccounts(Set.of(fromAccount));

        when(jwtUtil.extractUsername(anyString())).thenReturn("markezaa@example.com");
        when(clientRepository.findByEmail(anyString())).thenReturn(Optional.of(client));
        when(accountRepository.findAccountByAccountNumber(createPaymentDto.fromAccount()))
            .thenReturn(Optional.of(fromAccount));
        when(accountRepository.findAccountByAccountNumber(createPaymentDto.toAccount())).thenReturn(
            Optional.empty()
        );

        // Act & Assert
        assertThrows(
            AccountNotFound.class,
            () -> transactionService.createTransaction(authentication, createPaymentDto)
        );
    }

    @Test
    void testCreateTransactionFromAccountNotFound() {
        // Arrange
        CreatePaymentDto createPaymentDto = TransactionObjectMother.generateBasicCreatePaymentDto();
        Client client =
            ClientObjectMother.generateClient(
                UUID.fromString("9df5e618-f21d-48a7-a7a4-ac55ea8bec97"),
                "markezaa@example.com"
            );

        when(jwtUtil.extractUsername(anyString())).thenReturn("markezaa@example.com");
        when(clientRepository.findByEmail(anyString())).thenReturn(Optional.of(client));
        when(accountRepository.findAccountByAccountNumber(createPaymentDto.fromAccount()))
            .thenReturn(Optional.empty());
        when(accountRepository.findAccountByAccountNumber(createPaymentDto.toAccount())).thenReturn(
            Optional.of(AccountObjectMother.generateBasicToAccount())
        );

        // Act & Assert
        assertThrows(
            AccountNotFound.class,
            () -> transactionService.createTransaction(authentication, createPaymentDto)
        );
    }

    @Test
    void testCreateTransferSuccess() {
        // Arrange
        CreateTransferDto createTransferDto =
            TransactionObjectMother.generateBasicCreateTransferDto();
        Client client =
            ClientObjectMother.generateClient(
                UUID.fromString("9df5e618-f21d-48a7-a7a4-ac55ea8bec97"),
                "markezaa@example.com"
            );
        Account fromAccount = AccountObjectMother.generateBasicFromAccount();
        Account toAccount = AccountObjectMother.generateBasicToAccount();
        TransactionDto transactionDto = TransactionObjectMother.generateBasicTransactionDto();

        client.setAccounts(Set.of(fromAccount, toAccount));

        when(jwtUtil.extractUsername(anyString())).thenReturn("markezaa@example.com");
        when(clientRepository.findByEmail(anyString())).thenReturn(Optional.of(client));
        when(accountRepository.findAccountByAccountNumber(createTransferDto.fromAccount()))
            .thenReturn(Optional.of(fromAccount));
        when(accountRepository.findAccountByAccountNumber(createTransferDto.toAccount()))
            .thenReturn(Optional.of(toAccount));
        when(transactionMapper.toDto(any())).thenReturn(transactionDto);

        // Act
        TransactionDto result =
            transactionService.createTransfer(authentication, createTransferDto);

        // Assert
        assertNotNull(result);
        assertEquals(createTransferDto.fromAccount(), result.fromAccount());
        assertEquals(createTransferDto.toAccount(), result.toAccount());
        assertEquals(createTransferDto.fromAmount(), result.fromAmount());
        verify(transactionRepository, times(1)).save(any());
    }

}
