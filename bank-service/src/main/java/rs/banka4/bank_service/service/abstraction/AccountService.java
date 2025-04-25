package rs.banka4.bank_service.service.abstraction;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import rs.banka4.bank_service.domain.account.db.Account;
import rs.banka4.bank_service.domain.account.dtos.AccountDto;
import rs.banka4.bank_service.domain.account.dtos.CreateAccountDto;
import rs.banka4.bank_service.domain.account.dtos.SetAccountLimitsDto;
import rs.banka4.rafeisen.common.currency.CurrencyCode;
import rs.banka4.rafeisen.common.dto.AccountNumberDto;
import rs.banka4.rafeisen.common.security.AuthenticatedBankUserAuthentication;

public interface AccountService {
    Set<AccountDto> getAccountsForClient(String token);

    Set<AccountNumberDto> getAccountsForUser(UUID userId);

    AccountDto getAccount(String token, String id);

    Account getAccountByAccountNumber(String accountNumber);

    void createAccount(CreateAccountDto createAccountDto, String auth);

    void setAccountLimits(String accountNumber, SetAccountLimitsDto dto, String token);

    ResponseEntity<Page<AccountDto>> getAll(
        Authentication authentication,
        String firstName,
        String lastName,
        String accountNumber,
        PageRequest pageRequest
    );

    Account getAccount(AuthenticatedBankUserAuthentication auth, UUID id);

    void makeAnAccountNumber(Account account);

    Optional<AccountNumberDto> getRequiredAccount(
        UUID userId,
        CurrencyCode currencyCode,
        BigDecimal premium
    );
}
