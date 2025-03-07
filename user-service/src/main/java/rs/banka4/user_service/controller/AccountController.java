package rs.banka4.user_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.banka4.user_service.controller.docs.AccountApiDocumentation;
import rs.banka4.user_service.dto.AccountDto;
import rs.banka4.user_service.dto.requests.CreateAccountDto;
import rs.banka4.user_service.service.abstraction.AccountService;

import java.util.List;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController implements AccountApiDocumentation {

    private final AccountService accountService;

    @Override
    @GetMapping("/search")
    public ResponseEntity<Page<AccountDto>> getAll(Authentication auth,
                                                   @RequestParam(required = false) String firstName,
                                                   @RequestParam(required = false) String lastName,
                                                   @RequestParam(required = false) String accountNumber,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return accountService.getAll(auth, firstName, lastName, accountNumber, PageRequest.of(page, size));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccount(@PathVariable String id, Authentication auth) {
        return accountService.getAccount(auth.getCredentials().toString(), id);
    }

    @Override
    @GetMapping("/")
    public ResponseEntity<List<AccountDto>> getAccountsForClient(Authentication auth) {
        return accountService.getAccountsForClient(auth.getCredentials().toString());
    }

    @Override
    @GetMapping("/recipients")
    public ResponseEntity<List<AccountDto>> getRecentRecipients(Authentication auth) {
        return accountService.getRecentRecipientsFor(auth.getCredentials().toString());
    }

    @Override
    @PostMapping
    public ResponseEntity<Void> createAccount(@RequestBody @Valid CreateAccountDto createAccountDto, Authentication auth) {
        return accountService.createAccount(createAccountDto, (String) auth.getCredentials());
    }
}
