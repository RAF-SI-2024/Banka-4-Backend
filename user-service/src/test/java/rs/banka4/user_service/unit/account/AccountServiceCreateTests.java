package rs.banka4.user_service.unit.account;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.account.dtos.CreateAccountDto;
import rs.banka4.user_service.domain.company.db.Company;
import rs.banka4.user_service.domain.company.dtos.CompanyDto;
import rs.banka4.user_service.domain.currency.db.Currency;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.domain.user.employee.db.Employee;
import rs.banka4.user_service.exceptions.account.InvalidCurrency;
import rs.banka4.user_service.exceptions.company.CompanyNotFound;
import rs.banka4.user_service.exceptions.user.client.ClientNotFound;
import rs.banka4.user_service.exceptions.user.employee.EmployeeNotFound;
import rs.banka4.user_service.generator.AccountObjectMother;
import rs.banka4.user_service.generator.CompanyObjectMother;
import rs.banka4.user_service.repositories.AccountRepository;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.repositories.CurrencyRepository;
import rs.banka4.user_service.service.abstraction.ClientService;
import rs.banka4.user_service.service.abstraction.CompanyService;
import rs.banka4.user_service.service.abstraction.EmployeeService;
import rs.banka4.user_service.service.impl.AccountServiceImpl;
import rs.banka4.user_service.utils.JwtUtil;

public class AccountServiceCreateTests {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CurrencyRepository currencyRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private ClientService clientService;
    @Mock
    private CompanyService companyService;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateAccountSuccess() {
        // Arrange
        CreateAccountDto dto = AccountObjectMother.generateBasicCreateAccountDto();

        when(currencyRepository.findByCode(dto.currency())).thenReturn(
            AccountObjectMother.generateBasicFromAccount()
                .getCurrency()
        );
        when(
            clientRepository.findById(
                dto.client()
                    .id()
            )
        ).thenReturn(
            Optional.of(
                AccountObjectMother.generateBasicFromAccount()
                    .getClient()
            )
        );
        when(clientService.createClient(dto.client())).thenReturn(
            AccountObjectMother.generateBasicFromAccount()
                .getClient()
        );
        when(jwtUtil.extractUsername("authToken")).thenReturn("employee@example.com");
        when(employeeService.findEmployeeByEmail("employee@example.com")).thenReturn(
            Optional.of(
                AccountObjectMother.generateBasicFromAccount()
                    .getEmployee()
            )
        );

        // Act
        accountService.createAccount(dto, "authToken");

        // Assert
        verify(accountRepository, times(1)).save(any());
    }

    @Test
    void testCreateAccountWithInvalidCurrency() {
        // Arrange
        CreateAccountDto dto = AccountObjectMother.generateBasicCreateAccountDto();

        when(currencyRepository.findByCode(dto.currency())).thenReturn(null);
        when(
            clientRepository.findById(
                dto.client()
                    .id()
            )
        ).thenReturn(
            Optional.of(
                AccountObjectMother.generateBasicFromAccount()
                    .getClient()
            )
        );
        when(clientService.createClient(dto.client())).thenReturn(
            AccountObjectMother.generateBasicFromAccount()
                .getClient()
        );
        when(jwtUtil.extractUsername("authToken")).thenReturn("employee@example.com");
        when(employeeService.findEmployeeByEmail("employee@example.com")).thenReturn(
            Optional.of(
                AccountObjectMother.generateBasicFromAccount()
                    .getEmployee()
            )
        );

        // Act & Assert
        assertThrows(InvalidCurrency.class, () -> accountService.createAccount(dto, "authToken"));
    }

    @Test
    void testCreateAccountWithClientNotFound() {
        // Arrange
        CreateAccountDto dto = AccountObjectMother.generateBasicCreateAccountDto();
        when(currencyRepository.findByCode(dto.currency())).thenReturn(
            AccountObjectMother.generateBasicFromAccount()
                .getCurrency()
        );

        doThrow(new ClientNotFound("client@example.com")).when(clientService)
            .createClient(dto.client());

        when(jwtUtil.extractUsername("authToken")).thenReturn("employee@example.com");
        when(employeeService.findEmployeeByEmail("employee@example.com")).thenReturn(
            Optional.of(
                AccountObjectMother.generateBasicFromAccount()
                    .getEmployee()
            )
        );

        // Act & Assert
        assertThrows(ClientNotFound.class, () -> accountService.createAccount(dto, "authToken"));
    }

    @Test
    void testCreateAccountWithValidCompany() {
        // Arrange
        CreateAccountDto dto = AccountObjectMother.generateBasicCreateAccountDto();
        Currency currency =
            AccountObjectMother.generateBasicFromAccount()
                .getCurrency();
        Client client =
            AccountObjectMother.generateBasicFromAccount()
                .getClient();
        Employee employee =
            AccountObjectMother.generateBasicFromAccount()
                .getEmployee();
        CompanyDto companyDto = CompanyObjectMother.createCompanyDtoWithId();

        dto =
            new CreateAccountDto(
                dto.client(),
                companyDto,
                dto.availableBalance(),
                dto.currency(),
                dto.createCard()
            );

        when(currencyRepository.findByCode(dto.currency())).thenReturn(currency);
        when(
            clientRepository.findById(
                dto.client()
                    .id()
            )
        ).thenReturn(Optional.of(client));
        when(clientService.createClient(dto.client())).thenReturn(client);
        when(jwtUtil.extractUsername("authToken")).thenReturn("employee@example.com");
        when(employeeService.findEmployeeByEmail("employee@example.com")).thenReturn(
            Optional.of(employee)
        );
        when(
            companyService.getCompany(
                dto.company()
                    .id()
            )
        ).thenReturn(Optional.of(CompanyObjectMother.createCompanyEntityWithId(client)));

        // Act
        accountService.createAccount(dto, "authToken");

        // Assert
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void testCreateAccountWithInvalidCompany() {
        // Arrange
        CreateAccountDto dto = AccountObjectMother.generateBasicCreateAccountDto();
        Currency currency =
            AccountObjectMother.generateBasicFromAccount()
                .getCurrency();
        Client client =
            AccountObjectMother.generateBasicFromAccount()
                .getClient();
        Employee employee =
            AccountObjectMother.generateBasicFromAccount()
                .getEmployee();
        CompanyDto companyDto = CompanyObjectMother.createCompanyDtoWithId();

        dto =
            new CreateAccountDto(
                dto.client(),
                companyDto,
                dto.availableBalance(),
                dto.currency(),
                dto.createCard()
            );

        when(currencyRepository.findByCode(dto.currency())).thenReturn(currency);
        when(
            clientRepository.findById(
                dto.client()
                    .id()
            )
        ).thenReturn(Optional.of(client));
        when(clientService.createClient(dto.client())).thenReturn(client);
        when(jwtUtil.extractUsername("authToken")).thenReturn("employee@example.com");
        when(employeeService.findEmployeeByEmail("employee@example.com")).thenReturn(
            Optional.of(employee)
        );
        when(
            companyService.getCompany(
                dto.company()
                    .id()
            )
        ).thenReturn(Optional.empty());

        // Act & Assert
        CreateAccountDto finalDto = dto;
        assertThrows(
            CompanyNotFound.class,
            () -> accountService.createAccount(finalDto, "authToken")
        );
    }

    @Test
    void testCreateAccountWithEmployeeNotFound() {
        // Arrange
        CreateAccountDto dto = AccountObjectMother.generateBasicCreateAccountDto();
        Currency currency =
            AccountObjectMother.generateBasicFromAccount()
                .getCurrency();
        Client client =
            AccountObjectMother.generateBasicFromAccount()
                .getClient();

        when(currencyRepository.findByCode(dto.currency())).thenReturn(currency);
        when(
            clientRepository.findById(
                dto.client()
                    .id()
            )
        ).thenReturn(Optional.of(client));
        when(clientService.createClient(dto.client())).thenReturn(client);
        when(jwtUtil.extractUsername("authToken")).thenReturn("employee@example.com");
        when(employeeService.findEmployeeByEmail("employee@example.com")).thenReturn(
            Optional.empty()
        );

        // Act & Assert
        assertThrows(EmployeeNotFound.class, () -> accountService.createAccount(dto, "authToken"));
    }

    @Test
    void testCreateAccountWithDuplicateAccountNumber() {
        // Arrange
        CreateAccountDto dto = AccountObjectMother.generateBasicCreateAccountDto();
        Currency currency =
            AccountObjectMother.generateBasicFromAccount()
                .getCurrency();
        Client client =
            AccountObjectMother.generateBasicFromAccount()
                .getClient();
        Employee employee =
            AccountObjectMother.generateBasicFromAccount()
                .getEmployee();

        when(currencyRepository.findByCode(dto.currency())).thenReturn(currency);
        when(
            clientRepository.findById(
                dto.client()
                    .id()
            )
        ).thenReturn(Optional.of(client));
        when(clientService.createClient(dto.client())).thenReturn(client);
        when(jwtUtil.extractUsername("authToken")).thenReturn("employee@example.com");
        when(employeeService.findEmployeeByEmail("employee@example.com")).thenReturn(
            Optional.of(employee)
        );

        doThrow(new DataIntegrityViolationException("Duplicate account number")).doAnswer(
            invocation -> null
        )
            .when(accountRepository)
            .save(any(Account.class));

        // Act
        accountService.createAccount(dto, "authToken");

        // Assert
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    void testAccountNumberCreationBusinessType() {
        CreateAccountDto dto = AccountObjectMother.generateBusinessAccount();

        Currency currency =
            AccountObjectMother.generateBasicEURFromAccount()
                .getCurrency();
        Client client =
            AccountObjectMother.generateBasicFromAccount()
                .getClient();
        Employee employee =
            AccountObjectMother.generateBasicFromAccount()
                .getEmployee();
        Company company = CompanyObjectMother.createCompanyEntityWithId(client);

        when(currencyRepository.findByCode(dto.currency())).thenReturn(currency);
        when(
            clientRepository.findById(
                dto.client()
                    .id()
            )
        ).thenReturn(Optional.of(client));

        when(clientService.createClient(dto.client())).thenReturn(client);
        when(companyService.getCompany(anyString())).thenReturn(Optional.of(company));
        when(jwtUtil.extractUsername("authToken")).thenReturn("employee@example.com");
        when(employeeService.findEmployeeByEmail("employee@example.com")).thenReturn(
            Optional.of(employee)
        );

        accountService.createAccount(dto, "authToken");

    }
}
