package rs.banka4.user_service.service.impl;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
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
import rs.banka4.user_service.domain.account.dtos.AccountClientIdDto;
import rs.banka4.user_service.domain.account.dtos.AccountDto;
import rs.banka4.user_service.domain.account.dtos.CreateAccountDto;
import rs.banka4.user_service.domain.account.dtos.SetAccountLimitsDto;
import rs.banka4.user_service.domain.account.mapper.AccountMapper;
import rs.banka4.user_service.domain.card.dtos.CreateAuthorizedUserDto;
import rs.banka4.user_service.domain.card.dtos.CreateCardDto;
import rs.banka4.user_service.domain.company.db.Company;
import rs.banka4.user_service.domain.company.dtos.CreateCompanyDto;
import rs.banka4.user_service.domain.company.mapper.CompanyMapper;
import rs.banka4.user_service.domain.currency.db.Currency;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.domain.user.client.mapper.ClientMapper;
import rs.banka4.user_service.domain.user.employee.db.Employee;
import rs.banka4.user_service.exceptions.account.*;
import rs.banka4.user_service.exceptions.account.AccountNotFound;
import rs.banka4.user_service.exceptions.account.InvalidCurrency;
import rs.banka4.user_service.exceptions.company.CompanyNotFound;
import rs.banka4.user_service.exceptions.user.IncorrectCredentials;
import rs.banka4.user_service.exceptions.user.client.ClientNotFound;
import rs.banka4.user_service.exceptions.user.employee.EmployeeNotFound;
import rs.banka4.user_service.repositories.*;
import rs.banka4.user_service.service.abstraction.*;
import rs.banka4.user_service.utils.JwtUtil;
import rs.banka4.user_service.utils.specification.AccountSpecification;
import rs.banka4.user_service.utils.specification.SpecificationCombinator;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final ClientService clientService;
    private final ClientMapper clientMapper;
    private final CompanyService companyService;
    private final CurrencyRepository currencyRepository;
    private final CompanyMapper companyMapper;
    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final JwtUtil jwtUtil;
    private final EmployeeService employeeService;
    private final CardService cardService;


    @Override
    public Set<AccountDto> getAccountsForClient(String token) {
        String email = jwtUtil.extractUsername(token);

        Optional<Client> client = clientService.getClientByEmail(email);
        if (client.isEmpty()) {
            throw new ClientNotFound(email);
        }

        Set<Account> accounts = accountRepository.findAllByClient(client.get());
        return accounts.stream()
            .map(AccountMapper.INSTANCE::toDto)
            .collect(Collectors.toSet());
    }

    @Override
    public AccountDto getAccount(String token, String accountNumber) {
        String email = jwtUtil.extractUsername(token);
        Optional<Account> account = accountRepository.findAccountByAccountNumber(accountNumber);

        if (account.isEmpty()) {
            throw new AccountNotFound();
        }
        if (
            !email.equals(
                account.get()
                    .getClient()
                    .getEmail()
            )
        ) {
            throw new IncorrectCredentials();
        }

        return AccountMapper.INSTANCE.toDto(account.get());
    }

    /**
     * Creates a new account with the given details. If the client's id is set to null, a new client
     * will be created. If the client's id is available it will first check for it and then the
     * account will be created. Company functions in the similar way. If the company's object is set
     * to null then the Account will be set to {@link AccountType#STANDARD}. Otherwise, if the
     * company's id is given then it will first check for it and then the account will be made and
     * it will be set to {@link AccountType#DOO}. Currency will be set if the provided currency code
     * exists.
     *
     * @throws InvalidCurrency if the currency code doesn't exist
     * @throws CompanyNotFound if the company with the given id is not found
     * @throws ClientNotFound if the client with the given id is not found
     * @param createAccountDto the details of the account to be created
     * @param auth the authentication details of the client
     */
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
        makeAnAccountNumber(account);

        if (createAccountDto.createCard()) {
            cardService.createEmployeeCard(
                new CreateCardDto(account.getAccountNumber(), null, null),
                account
            );
        }
    }

    @Override
    public ResponseEntity<Page<AccountDto>> getAll(
        Authentication auth,
        String firstName,
        String lastName,
        String accountNumber,
        PageRequest pageRequest
    ) {
        String email =
            jwtUtil.extractUsername(
                auth.getCredentials()
                    .toString()
            );
        String role =
            jwtUtil.extractRole(
                auth.getCredentials()
                    .toString()
            );

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
        return accountRepository.findAccountByAccountNumber(accountNumber)
            .orElseThrow(AccountNotFound::new);
    }

    @Transactional
    public void setAccountLimits(String accountNumber, SetAccountLimitsDto dto, String token) {
        // Get account
        Account account =
            accountRepository.findAccountByAccountNumber(accountNumber)
                .orElseThrow(AccountNotFound::new);

        // Verify ownership
        String userId = jwtUtil.extractClaim(token, claims -> claims.get("id", String.class));
        if (
            !account.getClient()
                .getId()
                .toString()
                .equals(userId)
        ) {
            throw new NotAccountOwner();
        }

        // Check account status and expiration
        if (!account.isActive()) {
            throw new InvalidAccountOperation();
        }

        if (
            account.getExpirationDate()
                .isBefore(LocalDate.now())
        ) {
            throw new InvalidAccountOperation();
        }

        // Update limits
        if (dto.daily() != null) {
            account.setDailyLimit(dto.daily());
        }
        if (dto.monthly() != null) {
            account.setMonthlyLimit(dto.monthly());
        }

        accountRepository.save(account);
    }


    /**
     * Connects the given company to the account if the company is present in the database. If the
     * company is not present it will create a new company with the given details.
     *
     * @param account the account to be connected
     * @param createAccountDto with the details of the company given in
     *        {@link rs.banka4.user_service.domain.company.dtos.CompanyDto}
     * @throws CompanyNotFound if the company with the given id is not found
     */
    private void connectCompanyToAccount(Account account, CreateAccountDto createAccountDto) {
        if (createAccountDto.company() == null) return;

        if (
            createAccountDto.company()
                .id()
                == null
        ) {
            CreateCompanyDto createCompanyDto =
                companyMapper.toCreateDto(createAccountDto.company());
            companyService.createCompany(createCompanyDto, account.getClient());

            Optional<Company> company = companyService.getCompanyByCrn(createCompanyDto.crn());

            if (company.isPresent()) {
                account.setCompany(company.get());
            } else {
                throw new CompanyNotFound(
                    createAccountDto.company()
                        .crn()
                );
            }
        } else {
            Optional<Company> company =
                companyService.getCompany(
                    createAccountDto.company()
                        .id()
                );

            if (company.isPresent()) account.setCompany(company.get());
            else
                throw new CompanyNotFound(
                    createAccountDto.company()
                        .crn()
                );
        }

        account.setAccountType(AccountType.DOO);
    }


    /**
     * Connects a client to an account. If the client id is null, a new one will be created,
     * otherwise it will check for an existing client with the given id.
     *
     * @throws ClientNotFound if the given client id is not found
     * @param account the account to connect a client to
     * @param createAccountDto with the details of the client given in {@link AccountClientIdDto}
     */
    private void connectClientToAccount(Account account, CreateAccountDto createAccountDto) {
        if (
            createAccountDto.client()
                .id()
                == null
        ) {
            Client client = clientService.createClient(createAccountDto.client());
            account.setClient(client);
        } else {
            Optional<Client> client =
                clientRepository.findById(
                    createAccountDto.client()
                        .id()
                );

            if (client.isPresent()) {
                account.setClient(client.get());
            } else {
                throw new ClientNotFound(
                    createAccountDto.client()
                        .id()
                        .toString()
                );
            }
        }
    }

    /**
     * Connects the specified currency to the given account.
     *
     * @param account the account to which the currency is to be connected
     * @param createAccountDto contains the details of the currency code {@link Currency.Code}
     * @throws InvalidCurrency if the currency code does not exist in the repository
     */
    private void connectCurrencyToAccount(Account account, CreateAccountDto createAccountDto) {
        Currency currency = currencyRepository.findByCode(createAccountDto.currency());
        if (currency == null)
            throw new InvalidCurrency(
                createAccountDto.currency()
                    .toString()
            );

        account.setCurrency(currency);
        account.setAccountMaintenance();
    }

    /**
     * Connects the employee to the account from the given JWT token. The email of the employee is
     * extracted from the token and then the employee is found in the repository.
     *
     * @param account the account to connect the employee to
     * @param auth the JWT token from which the email is extracted
     * @throws EmployeeNotFound if the employee with the given email is not found
     */
    private void connectEmployeeToAccount(Account account, String auth) {
        String username = jwtUtil.extractUsername(auth);
        Optional<Employee> employee = employeeService.findEmployeeByEmail(username);

        if (employee.isEmpty()) {
            throw new EmployeeNotFound(username);
        } else {
            account.setEmployee(employee.get());
        }
    }

    /**
     * Generates a unique account number for the given account. The account number is 18 digits long
     * and follows the following format:
     * <ul>
     * <li>First 3 digits are 444 (bank code)</li>
     * <li>Next 4 digits are a number of the affiliated bank</li>
     * <li>Next 9 digits are a random number between 0 and 10^9 - 1</li>
     * <li>Last 2 digits are 10 for RSD and 20 for foreign currencies</li>
     * </ul>
     * The account number is generated until a unique number is found.
     *
     * @param account the account for which the account number is generated
     */
    private void makeAnAccountNumber(Account account) {
        String accountNumber = "";
        while (true) {
            try {
                long random =
                    ThreadLocalRandom.current()
                        .nextLong(0, (long) 1e10 - 1);
                accountNumber = String.format("4440001%09d", random);

                if(!account.getAccountMaintenance().equals(BigDecimal.ZERO)){
                    switch(account.getAccountType()){
                        case AccountType.STANDARD -> accountNumber+="11";
                        case AccountType.SAVINGS -> accountNumber+="12";
                        case AccountType.RETIREMENT -> accountNumber+="13";
                        case AccountType.YOUTH -> accountNumber+="14";
                        case AccountType.STUDENT -> accountNumber+="15";
                        default -> accountNumber+="16";
                    }
                }else{
                    switch(account.getAccountType()){
                        case AccountType.DOO -> accountNumber+="21";
                        default -> accountNumber+="22";
                    }
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

    private CreateAuthorizedUserDto mapClientToAuthorizedUser(AccountClientIdDto client) {
        return new CreateAuthorizedUserDto(
            client.firstName(),
            client.lastName(),
            client.dateOfBirth(),
            client.gender(),
            client.email(),
            client.phone(),
            client.address()
        );
    }

}
