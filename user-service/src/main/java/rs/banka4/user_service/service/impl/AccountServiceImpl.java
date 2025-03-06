package rs.banka4.user_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.dto.*;
import rs.banka4.user_service.dto.requests.CreateAccountDto;
import rs.banka4.user_service.models.*;
import rs.banka4.user_service.repositories.AccountRepository;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.service.abstraction.AccountService;
import rs.banka4.user_service.mapper.BasicAccountMapper;
import rs.banka4.user_service.utils.JwtUtil;
import rs.banka4.user_service.utils.specification.AccountSpecification;
import rs.banka4.user_service.utils.specification.EmployeeSpecification;
import rs.banka4.user_service.utils.specification.SpecificationCombinator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final JwtUtil jwtUtil;
    private final BasicAccountMapper accountMapper;
    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    CurrencyDto currencyDto = new CurrencyDto(
            "11111111-2222-3333-4444-555555555555",
            "Serbian Dinar",
            "RSD",
            "Currency used in Serbia",
            true,
            Currency.Code.RSD
    );

    CompanyDto companyDto = new CompanyDto(
            "cccccccc-4444-dddd-5555-eeee6666ffff",
            "Acme Corp",
            "123456789",
            "987654321",
            "123 Main St"
    );

    AccountDto account1 = new AccountDto(
            "11111111-2222-3333-4444-555555555555",
            "1234567890",
            new BigDecimal("1000.00"),
            new BigDecimal("800.00"),
            new BigDecimal("100.00"),
            LocalDate.of(2023, 1, 1),
            LocalDate.of(2028, 1, 1),
            true,
            AccountTypeDto.CheckingBusiness,
            new BigDecimal("100.00"),
            new BigDecimal("1000.00"),
            currencyDto,
            null,
            null,
            companyDto
    );

    AccountDto account2 = new AccountDto(
            "22222222-3333-4444-5555-666666666666",
            "0987654321",
            new BigDecimal("5000.00"),
            new BigDecimal("4500.00"),
            BigDecimal.ZERO, // Assuming maintenance is not applied here
            LocalDate.of(2022, 6, 15),
            LocalDate.of(2027, 6, 15),
            true,
            AccountTypeDto.FxBusiness,
            new BigDecimal("200.00"),
            new BigDecimal("5000.00"),
            currencyDto,
            null,
            null,
            companyDto
    );



    @Override
    public ResponseEntity<List<AccountDto>> getAccountsForClient(String token) {
        List<AccountDto> accounts = List.of(account1, account2);
        return ResponseEntity.ok(accounts);
    }

    @Override
    public ResponseEntity<AccountDto> getAccount(String token, String id){
        return ResponseEntity.ok(account1);
    }

    @Override
    public ResponseEntity<Void> createAccount(CreateAccountDto createAccountDto) {
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<Page<AccountDto>> getAll(Authentication auth, String firstName, String lastName, String accountNumber, PageRequest pageRequest) {
        String email = jwtUtil.extractUsername(auth.getCredentials().toString());
        String role = jwtUtil.extractRole(auth.getCredentials().toString());

        SpecificationCombinator<Account> combinator = new SpecificationCombinator<>();

        if (firstName != null && !firstName.isEmpty()) {
            combinator.and(AccountSpecification.hasFirstName(firstName));
        }
        if (lastName != null && !lastName.isEmpty()) {
            combinator.and(AccountSpecification.hasLastName(lastName));
        }
        if (accountNumber != null && !accountNumber.isEmpty()) {
            combinator.and(AccountSpecification.hasAccountNumber(accountNumber));
        }
        if (role.equals("client")) {
            combinator.and(AccountSpecification.hasEmail(email));
        }

        Page<Account> accounts = accountRepository.findAll(combinator.build(), pageRequest);

        return ResponseEntity.ok(accounts.map(accountMapper::toDto));
    }

    @Override
    public ResponseEntity<List<AccountDto>> getRecentRecipientsFor(String token) {
        List<AccountDto> accounts = List.of(account1, account2);
        return ResponseEntity.ok(accounts);
    }
}
