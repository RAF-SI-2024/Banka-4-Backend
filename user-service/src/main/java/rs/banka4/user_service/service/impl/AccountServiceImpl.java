package rs.banka4.user_service.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.account.db.AccountType;
import rs.banka4.user_service.domain.account.dtos.AccountDto;
import rs.banka4.user_service.domain.company.db.Company;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.domain.user.employee.db.Employee;
import rs.banka4.user_service.domain.account.dtos.CreateAccountDto;
import rs.banka4.user_service.domain.company.dtos.CreateCompanyDto;
import rs.banka4.user_service.domain.account.mapper.AccountMapper;
import rs.banka4.user_service.domain.company.mapper.CompanyMapper;
import rs.banka4.user_service.domain.currency.db.Currency;
import rs.banka4.user_service.exceptions.account.AccountNotFound;
import rs.banka4.user_service.exceptions.account.InvalidCurrency;
import rs.banka4.user_service.exceptions.user.IncorrectCredentials;
import rs.banka4.user_service.exceptions.user.NotFound;
import rs.banka4.user_service.exceptions.user.client.ClientNotFound;
import rs.banka4.user_service.exceptions.company.CompanyNotFound;
import rs.banka4.user_service.exceptions.user.employee.EmployeeNotFound;
import rs.banka4.user_service.repositories.*;
import rs.banka4.user_service.service.abstraction.AccountService;
import rs.banka4.user_service.service.abstraction.ClientService;
import rs.banka4.user_service.service.abstraction.CompanyService;
import rs.banka4.user_service.service.abstraction.EmployeeService;
import rs.banka4.user_service.utils.JwtUtil;
import rs.banka4.user_service.utils.specification.AccountSpecification;
import rs.banka4.user_service.utils.specification.SpecificationCombinator;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private static final Logger LOGGER
            = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final ClientService clientService;
    private final CompanyService companyService;
    private final CurrencyRepository currencyRepository;
    private final CompanyMapper companyMapper;
    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final JwtUtil jwtUtil;
    private final EmployeeService employeeService;

    @Override
    public Set<AccountDto> getAccountsForClient(String token) {
        String email = jwtUtil.extractUsername(token);

        Optional<Client> client = clientService.getClientByEmail(email);
        if (client.isEmpty()) {
            throw new ClientNotFound(email);
        }

        Set<Account> accounts = accountRepository.findAllByClient(client.get());
        return accounts.stream().map(AccountMapper.INSTANCE::toDto).collect(Collectors.toSet());
    }

    @Override
    public AccountDto getAccount(String token, String accountNumber) {
        String email = jwtUtil.extractUsername(token);
        Optional<Account> account = accountRepository.findAccountByAccountNumber(accountNumber);

        if (account.isEmpty()) {
            throw new AccountNotFound();
        }
        if (!email.equals(account.get().getClient().getEmail())) {
            throw new IncorrectCredentials();
        }

        return AccountMapper.INSTANCE.toDto(account.get());
    }

    @Transactional
    @Override
    public void createAccount(CreateAccountDto createAccountDto, String auth) {
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

        return ResponseEntity.ok(accounts.map(AccountMapper.INSTANCE::toDto));
    }

    @Override
    public Account getAccountByAccountNumber(String accountNumber) {
        return accountRepository.findAccountByAccountNumber(accountNumber).orElseThrow(AccountNotFound::new);
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
            Client client = clientService.createClient(createAccountDto.client());
            account.setClient(client);
        } else {
            Optional<Client> client = clientRepository.findById(createAccountDto.client().id());

            if (client.isPresent()) {
                account.setClient(client.get());
            } else {
                throw new ClientNotFound(createAccountDto.client().id().toString());
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
        Optional<Employee> employee = employeeService.findEmployeeByEmail(username);

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

                if(currency.equals(Currency.Code.RSD)) {
                    accountNumber += "10";
                } else {
                    accountNumber += "20";
                }
                account.setAccountNumber(accountNumber);
                account.setActive(true);
                accountRepository.save(account);

                break;
            } catch (DataIntegrityViolationException ex) {
                LOGGER.warn("Account with this account number already exists: {}", accountNumber);
            }
        }
    }
}
