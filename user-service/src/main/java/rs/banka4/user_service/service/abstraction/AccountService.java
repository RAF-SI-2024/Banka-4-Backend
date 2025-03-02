package rs.banka4.user_service.service.abstraction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.dto.AccountDto;
import rs.banka4.user_service.dto.requests.CreateAccountDto;

import java.util.List;
import java.util.UUID;

public interface AccountService {
    ResponseEntity<List<AccountDto>> getAccountsForClient(String token);
    ResponseEntity<List<AccountDto>> getRecentRecipientsFor(String token);
    ResponseEntity<AccountDto> getAccount(String token, String id);
    ResponseEntity<Void> createAccount(CreateAccountDto createAccountDto);
    ResponseEntity<Page<AccountDto>> getAll(String firstName, String lastName, String id, PageRequest pageRequest);
}
