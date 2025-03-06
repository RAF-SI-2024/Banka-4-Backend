package rs.banka4.user_service.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.dto.*;
import rs.banka4.user_service.dto.requests.CreateAccountDto;
import rs.banka4.user_service.dto.requests.CreateCompanyDto;
import rs.banka4.user_service.exceptions.*;
import rs.banka4.user_service.mapper.AccountMapper;
import rs.banka4.user_service.mapper.CompanyMapper;
import rs.banka4.user_service.models.*;
import rs.banka4.user_service.models.Currency;
import rs.banka4.user_service.repositories.*;
import rs.banka4.user_service.service.abstraction.AccountService;
import rs.banka4.user_service.service.abstraction.ClientService;
import rs.banka4.user_service.service.abstraction.CompanyService;
import rs.banka4.user_service.utils.JwtUtil;
import rs.banka4.user_service.utils.specification.AccountSpecification;
import rs.banka4.user_service.utils.specification.SpecificationCombinator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    private final ClientService clientService;
    private final CompanyService companyService;
    private final CurrencyRepository currencyRepository;
    private final CompanyMapper companyMapper;
    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final JwtUtil jwtUtil;
    private final EmployeeRepository employeeRepository;
    private final AccountMapper accountMapper;

    public AccountServiceImpl(@Lazy ClientService clientService, CompanyService companyService, CurrencyRepository currencyRepository, CompanyMapper companyMapper,
                              AccountRepository accountRepository, ClientRepository clientRepository, JwtUtil jwtUtil, EmployeeRepository employeeRepository, AccountMapper accountMapper) {
        this.clientService = clientService;
        this.companyService = companyService;
        this.currencyRepository = currencyRepository;
        this.companyMapper = companyMapper;
        this.accountRepository = accountRepository;
        this.clientRepository = clientRepository;
        this.jwtUtil = jwtUtil;
        this.employeeRepository = employeeRepository;
        this.accountMapper = accountMapper;
    }

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
            "123 Main St",
            "441100"
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
        String email = jwtUtil.extractUsername(token);

        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new ClientNotFound(email));

        List<Account> accounts = accountRepository.findAllByClient(client);

        if (accounts.isEmpty()) {
            throw new AccountNotFound();
        }

        List<AccountDto> accountDtos = accounts.stream()
                .map(accountMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(accountDtos);
    }


    @Override
    public ResponseEntity<AccountDto> getAccount(String token, String accoutNumber) {
        Optional<Account> account = accountRepository.findAccountByAccountNumber(accoutNumber);
        return ResponseEntity.ok(account.map(accountMapper::toDto).orElseThrow(AccountNotFound::new));
    }

    private void connectCompanyToAccount(Account account, CreateAccountDto createAccountDto) {
        if (createAccountDto.company() == null) return;

        if (createAccountDto.company().id() == null) {

            CreateCompanyDto createCompanyDto = companyMapper.toCreateDto(createAccountDto.company());
            companyService.createCompany(createCompanyDto, account.getClient());

            Optional<Company> company = companyService.getCompanyByCrn(createCompanyDto.crn());

            if (company.isPresent()) {
                account.setCompany(company.get());
            } else {
                throw new CompanyNotFound(createAccountDto.company().crn());
            }

        } else {
            Optional<Company> company = companyService.getCompany(createAccountDto.company().id());

            if (company.isPresent())
                account.setCompany(company.get());
            else
                throw new CompanyNotFound(createAccountDto.company().crn());
        }

        account.setAccountType(AccountType.DOO);
    }

    private void connectClientToAccount(Account account, CreateAccountDto createAccountDto) {

        if (createAccountDto.client().id() == null) {
            clientService.createClient(createAccountDto.client());
            Optional<Client> client = clientService.getClientByEmail(createAccountDto.client().email());

            if (client.isPresent()) {
                account.setClient(client.get());
            } else {
                throw new ClientNotFound(null);
            }

            account.setClient(client.get());
        } else {

            Optional<Client> client = clientRepository.findById(createAccountDto.client().id());

            if (client.isPresent()) {
                account.setClient(client.get());
            } else {
                throw new ClientNotFound(createAccountDto.client().id());
            }
        }
    }

    private void connectCurrencyToAccount(Account account, CreateAccountDto createAccountDto) {
        Currency currency = currencyRepository.findByCode(createAccountDto.currency());

        if (currency == null)
            throw new InvalidCurrency(createAccountDto.currency().toString());

        account.setCurrency(currency);
        account.setAccountMaintenance();
    }

    private void connectEmployeeToAccount(Account account, String auth) {
        String username = jwtUtil.extractUsername(auth);
        Optional<Employee> employee = employeeRepository.findByEmail(username);

        if (employee.isEmpty()) {
            throw new EmployeeNotFound(username);
        }
        else {
            account.setEmployee(employee.get());
        }
    }

    private void makeAnAccountNumber(Currency.Code currency,Account account){

        String accountNumber = "";

        while(true) {
            try {

                long random = ThreadLocalRandom.current().nextLong(0,(long) 1e10-1);

                accountNumber = String.format("4440001%09d", random);

                if(currency.equals(Currency.Code.RSD))
                    accountNumber+="10";
                else
                    accountNumber+="20";

                account.setAccountNumber(accountNumber);

                accountRepository.save(account);

                break;

            } catch (DataIntegrityViolationException ex) {
                System.out.println("Account with this account number already exists!" + accountNumber);
            }
        }
    }

    @Transactional
    @Override
    public ResponseEntity<Void> createAccount(CreateAccountDto createAccountDto, String auth) {
        Account account = new Account();

        connectClientToAccount(account, createAccountDto);

        if (createAccountDto.company() != null) {
            connectCompanyToAccount(account, createAccountDto);
        } else {
            account.setAccountType(AccountType.STANDARD);
        }

        connectCurrencyToAccount(account, createAccountDto);
        connectEmployeeToAccount(account, auth);
        account.setAvailableBalance(createAccountDto.availableBalance());
        account.setBalance(createAccountDto.availableBalance());
        makeAnAccountNumber(createAccountDto.currency(), account);

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

    @Override
    public Account getAccountByAccountNumber(String accountNumber) {
        return accountRepository.findAccountByAccountNumber(accountNumber).orElseThrow(NotFound::new);
    }
}
