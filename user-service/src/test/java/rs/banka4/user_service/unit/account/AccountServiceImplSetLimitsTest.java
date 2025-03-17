package rs.banka4.user_service.unit.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.account.dtos.SetAccountLimitsDto;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.exceptions.account.AccountNotFound;
import rs.banka4.user_service.exceptions.account.InvalidAccountOperation;
import rs.banka4.user_service.exceptions.account.NotAccountOwner;
import rs.banka4.user_service.repositories.AccountRepository;
import rs.banka4.user_service.service.impl.AccountServiceImpl;
import rs.banka4.user_service.utils.JwtUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplSetLimitsTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Account validAccount;
    private Client ownerClient;

    @BeforeEach
    void setUp() {
        ownerClient = Client.builder()
                .id(UUID.randomUUID())
                .email("owner@example.com")
                .build();

        validAccount = Account.builder()
                .id(UUID.randomUUID())
                .accountNumber("4440001123456789020")
                .active(true)
                .expirationDate(LocalDate.now().plusYears(1))
                .client(ownerClient)
                .dailyLimit(BigDecimal.ZERO)
                .monthlyLimit(BigDecimal.ZERO)
                .build();
    }

    @Test
    void setAccountLimits_Success() {
        // Arrange
        String accountNumber = "4440001123456789020";
        SetAccountLimitsDto dto = new SetAccountLimitsDto(
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(50000),
                "123456"
        );

        when(accountRepository.findAccountByAccountNumber(accountNumber))
                .thenReturn(Optional.of(validAccount));
        when(jwtUtil.extractClaim(any(), any()))
                .thenReturn(ownerClient.getId().toString());

        // Act
        accountService.setAccountLimits(accountNumber, dto, "valid.token");

        // Assert
        assertThat(validAccount.getDailyLimit()).isEqualByComparingTo(dto.daily());
        assertThat(validAccount.getMonthlyLimit()).isEqualByComparingTo(dto.monthly());
        verify(accountRepository).save(validAccount);
    }

    @Test
    void setAccountLimits_AccountNotFound() {
        // Arrange
        String accountNumber = "4440001123456789020";
        SetAccountLimitsDto dto = new SetAccountLimitsDto(
                BigDecimal.TEN,
                BigDecimal.TEN,
                "123456"
        );

        when(accountRepository.findAccountByAccountNumber(accountNumber))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> accountService.setAccountLimits(accountNumber, dto, "token"))
                .isInstanceOf(AccountNotFound.class);
    }

    @Test
    void setAccountLimits_UnauthorizedAccess() {
        // Arrange
        String accountNumber = "4440001123456789020";
        SetAccountLimitsDto dto = new SetAccountLimitsDto(
                BigDecimal.TEN,
                BigDecimal.TEN,
                "123456"
        );

        when(accountRepository.findAccountByAccountNumber(accountNumber))
                .thenReturn(Optional.of(validAccount));
        when(jwtUtil.extractClaim(any(), any()))
                .thenReturn(UUID.randomUUID().toString());

        // Act & Assert
        assertThatThrownBy(() -> accountService.setAccountLimits(accountNumber, dto, "invalid.token"))
                .isInstanceOf(NotAccountOwner.class);
    }

    @Test
    void setAccountLimits_AccountInactive() {
        // Arrange
        String accountNumber = "4440001123456789020";
        validAccount.setActive(false);
        SetAccountLimitsDto dto = new SetAccountLimitsDto(
                BigDecimal.TEN,
                BigDecimal.TEN,
                "123456"
        );

        when(accountRepository.findAccountByAccountNumber(accountNumber))
                .thenReturn(Optional.of(validAccount));
        when(jwtUtil.extractClaim(any(), any()))
                .thenReturn(ownerClient.getId().toString());

        // Act & Assert
        assertThatThrownBy(() -> accountService.setAccountLimits(accountNumber, dto, "valid.token"))
                .isInstanceOf(InvalidAccountOperation.class);
    }

    @Test
    void setAccountLimits_AccountExpired() {
        // Arrange
        String accountNumber = "4440001123456789020";
        validAccount.setExpirationDate(LocalDate.now().minusDays(1));
        SetAccountLimitsDto dto = new SetAccountLimitsDto(
                BigDecimal.TEN,
                BigDecimal.TEN,
                "123456"
        );

        when(accountRepository.findAccountByAccountNumber(accountNumber))
                .thenReturn(Optional.of(validAccount));
        when(jwtUtil.extractClaim(any(), any()))
                .thenReturn(ownerClient.getId().toString());

        // Act & Assert
        assertThatThrownBy(() -> accountService.setAccountLimits(accountNumber, dto, "valid.token"))
                .isInstanceOf(InvalidAccountOperation.class);
    }

    @Test
    void setAccountLimits_PartialUpdate() {
        // Arrange
        String accountNumber = "4440001123456789020";
        validAccount.setDailyLimit(BigDecimal.valueOf(1000));
        validAccount.setMonthlyLimit(BigDecimal.valueOf(10000));

        SetAccountLimitsDto dto = new SetAccountLimitsDto(
                null,
                BigDecimal.valueOf(20000),
                "123456"
        );

        when(accountRepository.findAccountByAccountNumber(accountNumber))
                .thenReturn(Optional.of(validAccount));
        when(jwtUtil.extractClaim(any(), any()))
                .thenReturn(ownerClient.getId().toString());

        // Act
        accountService.setAccountLimits(accountNumber, dto, "valid.token");

        // Assert
        assertThat(validAccount.getDailyLimit()).isEqualByComparingTo("1000"); // unchanged
        assertThat(validAccount.getMonthlyLimit()).isEqualByComparingTo("20000"); // updated
    }
}