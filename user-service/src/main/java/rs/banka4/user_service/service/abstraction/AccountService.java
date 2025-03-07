package rs.banka4.user_service.service.abstraction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import rs.banka4.user_service.domain.account.dtos.AccountDto;
import rs.banka4.user_service.domain.account.dtos.CreateAccountDto;
import rs.banka4.user_service.domain.account.db.Account;

import java.util.List;

public interface AccountService {
    ResponseEntity<List<AccountDto>> getAccountsForClient(String token);
    ResponseEntity<List<AccountDto>> getRecentRecipientsFor(String token);
    ResponseEntity<AccountDto> getAccount(String token, String id);
    Account getAccountByAccountNumber(String accountNumber);
    ResponseEntity<Void> createAccount(CreateAccountDto createAccountDto, String auth);
    ResponseEntity<Page<AccountDto>> getAll(Authentication authentication, String firstName, String lastName, String accountNumber, PageRequest pageRequest);
}
