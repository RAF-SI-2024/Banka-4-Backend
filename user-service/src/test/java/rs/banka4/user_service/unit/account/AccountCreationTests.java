package rs.banka4.user_service.unit.account;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.dto.ClientDto;
import rs.banka4.user_service.dto.CompanyDto;
import rs.banka4.user_service.dto.EmployeeResponseDto;
import rs.banka4.user_service.dto.requests.CreateAccountDto;
import rs.banka4.user_service.dto.requests.CreateClientDto;
import rs.banka4.user_service.dto.requests.CreateCompanyDto;
import rs.banka4.user_service.exceptions.ClientNotFound;
import rs.banka4.user_service.exceptions.CompanyNotFound;
import rs.banka4.user_service.exceptions.EmployeeNotFound;
import rs.banka4.user_service.exceptions.InvalidCurrency;
import rs.banka4.user_service.mapper.ClientMapper;
import rs.banka4.user_service.mapper.CompanyMapper;
import rs.banka4.user_service.mapper.EmployeeMapper;
import rs.banka4.user_service.models.*;
import rs.banka4.user_service.models.Currency;
import rs.banka4.user_service.repositories.AccountRepository;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.repositories.CurrencyRepository;
import rs.banka4.user_service.repositories.EmployeeRepository;
import rs.banka4.user_service.service.abstraction.AccountService;
import rs.banka4.user_service.service.abstraction.ClientService;
import rs.banka4.user_service.service.abstraction.CompanyService;
import rs.banka4.user_service.service.abstraction.EmployeeService;
import rs.banka4.user_service.service.impl.AccountServiceImpl;
import rs.banka4.user_service.utils.JwtUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AccountCreationTests {

    @Mock
    private ClientService clientService;

    @Mock
    private CompanyService companyService;

    @Mock
    private ClientMapper clientMapper;

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CompanyMapper companyMapper;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private EmployeeMapper employeeMapper;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private AccountServiceImpl accountService;

    private CreateAccountDto createAccountDto;

    private ClientDto mockClient;

    private CreateClientDto mockCreateClient;

    private CompanyDto mockCompany;

    private CreateClientDto createClientDto;


    @BeforeEach
    void setUp() {
        mockCreateClient = new CreateClientDto("12345", "John",
                "Doe",
                LocalDate.now(),
                "Male",
                "john.doe@example.com",
                "12313",
                "dasasd",
                EnumSet.of(Privilege.SEARCH));

        mockCompany = new CompanyDto(
                "123123",
                "Acme Corp",
                "123456789",
                "987654321",
                "123 Some Address",
                "4441100"
        );

        createAccountDto = new CreateAccountDto(mockCreateClient,
                mockCompany,new BigDecimal("153247.75") ,
                Currency.Code.RSD);

         createClientDto = new CreateClientDto("123","John",
                "Doe",
                LocalDate.now(),
                "Male",
                "john.doe@example.com",
                "12313",
                "dasasd",
                Set.of(Privilege.SEARCH)
        );

        MockitoAnnotations.openMocks(this);
    }


    @Test
    public void shouldThrowExceptionWhenClientIdDoesNotExist() {
        when(clientMapper.toCreateDto(any())).thenReturn(createClientDto);
        when(clientRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(ClientNotFound.class, () -> accountService.createAccount(createAccountDto,anyString()));
    }

    @Test
    void createAccount_Success_OldClientOldCompany_ValidCurrency() {
        CreateClientDto createClientDto = new CreateClientDto("123", "John",
                "Doe",
                LocalDate.now(),
                "Male",
                "john.doe@example.com",
                "12313",
                "dasasd",
                Set.of(Privilege.SEARCH)
        );


        when(clientMapper.toCreateDto(mockClient)).thenReturn(createClientDto);


        CreateCompanyDto createCompanyDto = new CreateCompanyDto(
                "Acme Corp",
                "123456789",
                "987654321",
                "123 Some Address", "1222");

        when(companyMapper.toCreateDto(mockCompany)).thenReturn(createCompanyDto);


        var newClient = new Client();
        newClient.setId("clientIdXYZ");
        newClient.setEmail("jane.doe@example.com");
        when(clientRepository.findById(anyString()))
                .thenReturn(Optional.of(newClient));


        var newCompany = new Company();
        newCompany.setId(UUID.randomUUID());
        newCompany.setName("Acme Corp");
        newCompany.setTin("123456789");
        newCompany.setCrn("987654321");
        when(companyService.getCompanyByCrn(anyString())).thenReturn(Optional.of(newCompany));

        when(companyService.getCompany(anyString())).thenReturn(Optional.of(newCompany));


        var currency = new Currency();
        currency.setCode(Currency.Code.RSD);
        when(currencyRepository.findByCode(Currency.Code.RSD)).thenReturn(currency);


        var employeeEntity = new Employee();
        employeeEntity.setId("empId");
        employeeEntity.setEmail("john.smith@example.com");
        when(jwtUtil.extractUsername("jwt")).thenReturn(employeeEntity.email);
        when(employeeRepository.findByEmail(employeeEntity.email)).thenReturn(Optional.of(employeeEntity));


        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });


        ResponseEntity<Void> response = accountService.createAccount(createAccountDto, "jwt");


        assertEquals(201, response.getStatusCodeValue());

        verify(accountRepository, atLeastOnce()).save(any(Account.class));


        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository, atLeastOnce()).save(accountCaptor.capture());
        Account savedAcc = accountCaptor.getValue();


        assertNotNull(savedAcc.getClient());
        assertEquals("clientIdXYZ", savedAcc.getClient().getId());
        assertNotNull(savedAcc.getCompany());
        assertEquals("Acme Corp", savedAcc.getCompany().getName());


        assertEquals(Currency.Code.RSD, savedAcc.getCurrency().getCode());
        assertEquals(AccountType.DOO, savedAcc.getAccountType());


        assertEquals(BigDecimal.valueOf(153247.75), savedAcc.getAvailableBalance());
        assertEquals(BigDecimal.valueOf(0), savedAcc.getDailyLimit());
        assertEquals(BigDecimal.valueOf(0), savedAcc.getMonthlyLimit());


        assertNotNull(savedAcc.getEmployee());
        assertEquals("empId", savedAcc.getEmployee().getId());


        assertNotNull(savedAcc.getAccountNumber());
    }

    @Test
    void createAccount_ClientNotFound() {
        var employeeEntity = new Employee();
        employeeEntity.setId("empId");
        employeeEntity.setEmail("john.smith@example.com");
        when(jwtUtil.extractUsername("jwt")).thenReturn(employeeEntity.email);
        when(employeeRepository.findByEmail(employeeEntity.email)).thenReturn(Optional.of(employeeEntity));

        when(clientRepository.findById(anyString()))
                .thenReturn(Optional.empty());


        when(currencyRepository.findByCode(createAccountDto.currency())).thenReturn(new Currency());
        EmployeeResponseDto empResp = new EmployeeResponseDto(
                "empId","John","Smith",LocalDate.of(1985,5,20),
                "Male","john.smith@example.com","555-4321","Employee Address",
                null,"username","IT",EnumSet.of(Privilege.SEARCH),true
        );
        when(employeeService.getMe(anyString())).thenReturn(ResponseEntity.ok(empResp));
        when(employeeMapper.toEntity(empResp)).thenReturn(new Employee());


        assertThrows(ClientNotFound.class, () -> accountService.createAccount(createAccountDto, "authToken"));
    }


    @Test
    void createAccount_CompanyNotFound() {
        var employeeEntity = new Employee();
        employeeEntity.setId("empId");
        employeeEntity.setEmail("john.smith@example.com");
        when(jwtUtil.extractUsername("jwt")).thenReturn(employeeEntity.email);
        when(employeeRepository.findByEmail(employeeEntity.email)).thenReturn(Optional.of(employeeEntity));

        var newClient = new Client();
        newClient.setId("clientIdXYZ");
        newClient.setEmail("jane.doe@example.com");
        when(clientRepository.findById(anyString()))
                .thenReturn(Optional.of(newClient));

        when(companyService.getCompanyByCrn(anyString())).thenReturn(Optional.empty());


        when(currencyRepository.findByCode(createAccountDto.currency())).thenReturn(new Currency());



        assertThrows(CompanyNotFound.class, () -> accountService.createAccount(createAccountDto, "authToken"));
    }


    @Test
    void createAccount_InvalidCurrency() {

        var employeeEntity = new Employee();
        employeeEntity.setId("empId");
        employeeEntity.setEmail("john.smith@example.com");
        when(jwtUtil.extractUsername("jwt")).thenReturn(employeeEntity.email);
        when(employeeRepository.findByEmail(employeeEntity.email)).thenReturn(Optional.of(employeeEntity));

        var newClient = new Client();
        newClient.setId("clientIdXYZ");
        newClient.setEmail("jane.doe@example.com");
        when(clientRepository.findById(anyString()))
                .thenReturn(Optional.of(newClient));

        CreateCompanyDto createCompanyDto = new CreateCompanyDto(
                "Acme Corp",
                "123456789",
                "987654321",
                "123 Some Address", "12222");

        when(companyMapper.toCreateDto(mockCompany)).thenReturn(createCompanyDto);


        var newCompany = new Company();
        newCompany.setId(UUID.randomUUID());
        newCompany.setName("Acme Corp");
        newCompany.setTin("123456789");
        newCompany.setCrn("987654321");
        when(companyService.getCompanyByCrn(anyString())).thenReturn(Optional.of(newCompany));

        when(companyService.getCompany(anyString())).thenReturn(Optional.of(newCompany));


        when(currencyRepository.findByCode(createAccountDto.currency())).thenReturn(null);


        assertThrows(InvalidCurrency.class, () -> accountService.createAccount(createAccountDto, "authToken"));
    }


    @Test
    void createAccount_EmployeeNotFound() {
        var employeeEntity = new Employee();
        employeeEntity.setId("empId");
        employeeEntity.setEmail("john.smith@example.com");
        when(jwtUtil.extractUsername("jwt")).thenReturn(employeeEntity.email);
        when(employeeRepository.findByEmail(employeeEntity.email)).thenReturn(Optional.empty());

        var newClient = new Client();
        newClient.setId("clientIdXYZ");
        newClient.setEmail("jane.doe@example.com");
        when(clientRepository.findById(anyString()))
                .thenReturn(Optional.of(newClient));

        CreateCompanyDto createCompanyDto = new CreateCompanyDto(
                "Acme Corp",
                "123456789",
                "987654321",
                "123 Some Address", "12222");

        when(companyMapper.toCreateDto(mockCompany)).thenReturn(createCompanyDto);

        var newCompany = new Company();
        newCompany.setId(UUID.randomUUID());
        newCompany.setName("Acme Corp");
        newCompany.setTin("123456789");
        newCompany.setCrn("987654321");
        when(companyService.getCompanyByCrn(anyString())).thenReturn(Optional.of(newCompany));

        when(companyService.getCompany(anyString())).thenReturn(Optional.of(newCompany));


        when(currencyRepository.findByCode(createAccountDto.currency())).thenReturn(new Currency());

        when(companyService.getCompanyByCrn(anyString())).thenReturn(Optional.of(new Company()));


        assertThrows(EmployeeNotFound.class, () -> accountService.createAccount(createAccountDto, "jwt"));
    }


    @Test
    void createAccount_DataIntegrity_SuccessAfterRetry() {

        var employeeEntity = new Employee();
        employeeEntity.setId("empId");
        employeeEntity.setEmail("john.smith@example.com");
        when(jwtUtil.extractUsername("jwt")).thenReturn(employeeEntity.email);
        when(employeeRepository.findByEmail(employeeEntity.email)).thenReturn(Optional.of(employeeEntity));

        var newClient = new Client();
        newClient.setId("clientIdXYZ");
        newClient.setEmail("jane.doe@example.com");
        when(clientRepository.findById(anyString()))
                .thenReturn(Optional.of(newClient));

        CreateCompanyDto createCompanyDto = new CreateCompanyDto(
                "Acme Corp",
                "123456789",
                "987654321",
                "123 Some Address", "12222");

        when(companyMapper.toCreateDto(mockCompany)).thenReturn(createCompanyDto);


        var newCompany = new Company();
        newCompany.setId(UUID.randomUUID());
        newCompany.setName("Acme Corp");
        newCompany.setTin("123456789");
        newCompany.setCrn("987654321");
        when(companyService.getCompanyByCrn(anyString())).thenReturn(Optional.of(newCompany));
        when(companyService.getCompany(anyString())).thenReturn(Optional.of(newCompany));
        when(currencyRepository.findByCode(createAccountDto.currency())).thenReturn(new Currency());
        when(companyService.getCompanyByCrn(anyString())).thenReturn(Optional.of(new Company()));


        final int[] saveCounter = {0};
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            saveCounter[0]++;
            if (saveCounter[0] == 1) {
                throw new DataIntegrityViolationException("Duplicate account number!");
            }

            Account account = invocation.getArgument(0);
            account.setId(UUID.randomUUID());
            return account;
        });


        ResponseEntity<Void> response = accountService.createAccount(createAccountDto, "jwt");


        assertEquals(HttpStatusCode.valueOf(201), response.getStatusCode());
        verify(accountRepository, atLeast(2)).save(any(Account.class));

    }


    @Test
    void createAccount_Success_NewClientNewCompany_ValidCurrency() {

        var mockCompany = new CompanyDto(
                null,
                "Acme Corp",
                "123456789",
                "987654321",
                "123 Some Address", "12222"
        );

        var createAccountDto = new CreateAccountDto(mockCreateClient,
                mockCompany,new BigDecimal("153247.75") ,
                Currency.Code.RSD);

        var createClientDto = new CreateClientDto("123","John",
                "Doe",
                LocalDate.now(),
                "Male",
                "john.doe@example.com",
                "12313",
                "dasasd",
                Set.of(Privilege.SEARCH)
        );

        CreateCompanyDto createCompanyDto = new CreateCompanyDto(
                "Acme Corp",
                "123456789",
                "987654321",
                "123 Some Address",
                "12222"
        );

        when(clientMapper.toCreateDto(any(ClientDto.class))).thenReturn(createClientDto);
        var newClient = new Client();
        newClient.setId("clientIdXYZ");
        newClient.setEmail("jane.doe@example.com");
        when(clientService.getClientByEmail(anyString()))
                .thenReturn(Optional.of(newClient));

        when(clientRepository.findById(anyString())).thenReturn(Optional.of(newClient));


        var employeeEntity = new Employee();
        employeeEntity.setId("empId");
        employeeEntity.setEmail("john.smith@example.com");
        when(jwtUtil.extractUsername("jwt")).thenReturn(employeeEntity.email);
        when(employeeRepository.findByEmail(employeeEntity.email)).thenReturn(Optional.of(employeeEntity));


        when(companyMapper.toCreateDto(any(CompanyDto.class))).thenReturn(createCompanyDto);

        var newCompany = new Company();
        newCompany.setId(UUID.randomUUID());
        newCompany.setName("Acme Corp");
        newCompany.setTin("123456789");
        newCompany.setCrn("987654321");
        when(companyService.getCompanyByCrn(anyString())).thenReturn(Optional.of(newCompany));

        when(companyService.getCompany(anyString())).thenReturn(Optional.of(newCompany));


        var currency = new Currency();
        currency.setCode(Currency.Code.RSD);
        when(currencyRepository.findByCode(Currency.Code.RSD)).thenReturn(currency);



        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });


        ResponseEntity<Void> response = accountService.createAccount(createAccountDto, "jwt");
        
        assertEquals(201, response.getStatusCodeValue());

        verify(accountRepository, atLeastOnce()).save(any(Account.class));


        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository, atLeastOnce()).save(accountCaptor.capture());
        Account savedAcc = accountCaptor.getValue();


        assertNotNull(savedAcc.getClient());
        assertEquals("clientIdXYZ", savedAcc.getClient().getId());
        assertNotNull(savedAcc.getCompany());
        assertEquals("Acme Corp", savedAcc.getCompany().getName());


        assertEquals(Currency.Code.RSD, savedAcc.getCurrency().getCode());
        assertEquals(AccountType.DOO, savedAcc.getAccountType());


        assertEquals(BigDecimal.valueOf(153247.75), savedAcc.getAvailableBalance());
        assertEquals(BigDecimal.valueOf(0), savedAcc.getDailyLimit());
        assertEquals(BigDecimal.valueOf(0), savedAcc.getMonthlyLimit());


        assertNotNull(savedAcc.getEmployee());
        assertEquals("empId", savedAcc.getEmployee().getId());


        assertNotNull(savedAcc.getAccountNumber());
    }


}
