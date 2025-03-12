package rs.banka4.user_service.service.abstraction;

import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.account.dtos.AccountDto;
import rs.banka4.user_service.domain.account.dtos.CreateAccountDto;

public interface AccountService {
    Set<AccountDto> getAccountsForClient(String token);

    AccountDto getAccount(String token, String id);

    Account getAccountByAccountNumber(String accountNumber);

    void createAccount(CreateAccountDto createAccountDto, String auth);

    ResponseEntity<Page<AccountDto>> getAll(
        Authentication authentication,
        String firstName,
        String lastName,
        String accountNumber,
        PageRequest pageRequest
    );

}
