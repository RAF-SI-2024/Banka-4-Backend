package rs.banka4.user_service.runners;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import rs.banka4.rafeisen.common.currency.CurrencyCode;
import rs.banka4.rafeisen.common.dto.Gender;
import rs.banka4.rafeisen.common.security.Privilege;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.account.db.AccountType;
import rs.banka4.user_service.domain.card.db.*;
import rs.banka4.user_service.domain.company.db.ActivityCode;
import rs.banka4.user_service.domain.company.db.Company;
import rs.banka4.user_service.domain.currency.db.Currency;
import rs.banka4.user_service.domain.loan.db.*;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.domain.user.client.db.ClientContact;
import rs.banka4.user_service.domain.user.employee.db.Employee;
import rs.banka4.user_service.repositories.*;


@Component
@RequiredArgsConstructor
public class TestDataRunner implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestDataRunner.class);
    private final Environment environment;

    private static final UUID EMPLOYEE_ALICE =
        UUID.fromString("557514F1-2740-4C50-88CA-BE8235C1C4F3");
    private static final UUID EMPLOYEE_ROBERT =
        UUID.fromString("D9BF337A-C052-43F3-947F-1CDDE9B48CAC");
    private static final UUID EMPLOYEE_DANIEL =
        UUID.fromString("2A3EB11D-8686-4B24-92DC-273A88131548");
    private static final UUID EMPLOYEE_SAMANTHA =
        UUID.fromString("C98DCD32-F0FF-4C0D-9D48-F0023844D984");
    private static final UUID EMPLOYEE_JESSICA =
        UUID.fromString("7CE33D5F-11F5-490E-A21D-564FA3ADC3C7");
    private static final UUID EMPLOYEE_MICHAEL =
        UUID.fromString("4F7F5481-A667-47EE-8650-E6D8A2BC3616");
    private static final UUID EMPLOYEE_LAURA =
        UUID.fromString("3B086FD6-1991-4273-8CA3-F609458D3DDD");
    private static final UUID EMPLOYEE_DAVID =
        UUID.fromString("9F617476-BDCE-40AC-8999-E942890F0626");
    private static final UUID EMPLOYEE_EMMA =
        UUID.fromString("58C441D9-1A1D-4F21-8736-6E784BFE394E");
    private static final UUID EMPLOYEE_CHRIS =
        UUID.fromString("D55C2670-F40D-4DF6-B06A-0CADB80AE023");

    private static final UUID CLIENT_JOHN = UUID.fromString("D96CBD90-4DA2-44C4-866C-E0E37754F95D");
    private static final UUID CLIENT_JANE = UUID.fromString("44ADDAB9-F74D-4974-9733-4B23E6D4DCC9");
    private static final UUID CLIENT_DANIEL =
        UUID.fromString("B578A349-4271-4A22-8F10-DAE69DDFBB45");
    private static final UUID CLIENT_BANK_SELF =
        UUID.fromString("723B12AF-9DF4-4A9D-BE33-B054B02C7D90");

    private static final UUID COMPANY_BIG_COMPANY_DOO =
        UUID.fromString("259A9DFB-E5A6-46F0-AAD8-5E29496503C0");
    private static final UUID COMPANY_RAFFEISEN_BANK =
        UUID.fromString("073F00D9-D258-4CF2-8559-BADCDF0D1350");
    private static final UUID STATE_COMPANY =
        UUID.fromString("6B730AA0-0E1C-11F0-B4C8-0800200C9A66");

    private static final UUID LOAN_INTEREST_0_500000 =
        UUID.fromString("B0513B81-D32E-4F64-A9BF-D939AFE0E6A6");
    private static final UUID LOAN_INTEREST_500001_1000000 =
        UUID.fromString("EF29BB2E-1C65-4C76-98D9-CA7FBCF2C6BB");
    private static final UUID LOAN_INTEREST_1000001_2000000 =
        UUID.fromString("9A52A003-06F9-4023-B18D-D06F6D857ADF");
    private static final UUID LOAN_INTEREST_2000001_5000000 =
        UUID.fromString("8EF87FA5-BA82-4A56-A5BB-AFC1C652225F");
    private static final UUID LOAN_INTEREST_5000001_10000000 =
        UUID.fromString("E0DB80D4-2244-483D-9D94-E11C8B7641E6");
    private static final UUID LOAN_INTEREST_10000001_20000000 =
        UUID.fromString("68B85D72-EB30-4136-A397-7E6D25DB8EED");
    private static final UUID LOAN_INTEREST_20000001_2000000100 =
        UUID.fromString("E533B80B-B1F4-4B20-93D1-66B70401DE56");

    private static final UUID BANK_MARGIN_CASH =
        UUID.fromString("EA440E8A-B516-468E-B942-D3E5C208F0DD");
    private static final UUID BANK_MARGIN_MORTGAGE =
        UUID.fromString("6F01B5DC-ECF8-446E-B561-5720376C3E5A");
    private static final UUID BANK_MARGIN_AUTO_LOAN =
        UUID.fromString("69D72FF5-0018-4B71-8DAE-73797E5CBF52");
    private static final UUID BANK_MARGIN_REFINANCING =
        UUID.fromString("D2A4F4D1-181C-46AF-9675-292699C980FC");
    private static final UUID BANK_MARGIN_STUDENT_LOAN =
        UUID.fromString("AE8A5DCB-2ED9-4D04-A23A-DD49F4C3B47A");

    private static final UUID CARD_STANDARD =
        UUID.fromString("1A38A9BD-7231-402D-9A9C-4927953C23F3");
    private static final String CARD_STANDARD_NUMBER = "1234567810345678";
    private static final String CARD_STANDARD_CVV = "123";

    private static final UUID CARD_DOO = UUID.fromString("0ED4C23C-DEAF-4D44-A5BA-6BC1E48BC315");
    private static final UUID CARD_DOO_AUTHORIZED_USER =
        UUID.fromString("54F5792B-95B7-4E26-8E13-87A5A1520244");
    private static final String CARD_DOO_NUMBER = "8765432107654321";
    private static final String CARD_DOO_CVV = "321";

    private static final UUID STATE_ACCOUNT =
        UUID.fromString("1F969C90-0E1D-11F0-B4C8-0800200C9A66");
    private static final String STATE_ACCOUNT_NUMBER = "4440001000000000999";

    private static final UUID BANK_ACCOUNT_RSD =
        UUID.fromString("8142AA92-D71E-461C-AD92-A3228FE46488");
    private static final String BANK_ACCOUNT_RSD_NUMBER = "4440001000000000010";

    private static final UUID BANK_ACCOUNT_EUR =
        UUID.fromString("FBA778B9-063F-4C99-B00B-B5AC43CB464C");
    private static final String BANK_ACCOUNT_EUR_NUMBER = "4440001000000000020";

    private static final UUID BANK_ACCOUNT_USD =
        UUID.fromString("2EE2F6BC-3BBD-409B-80C2-DA4C8D605BA2");
    private static final String BANK_ACCOUNT_USD_NUMBER = "4440001000000000120";

    private static final UUID BANK_ACCOUNT_CHF =
        UUID.fromString("20558201-FF0C-405C-BE04-4185DCB3D11E");
    private static final String BANK_ACCOUNT_CHF_NUMBER = "4440001000000000220";

    private static final UUID BANK_ACCOUNT_JPY =
        UUID.fromString("24A27A98-E983-4524-9949-3FA62414E05D");
    private static final String BANK_ACCOUNT_JPY_NUMBER = "4440001000000000320";

    private static final UUID BANK_ACCOUNT_AUD =
        UUID.fromString("356388C5-5E16-4158-A9F5-A2A0A516E93B");
    private static final String BANK_ACCOUNT_AUD_NUMBER = "4440001000000000420";

    private static final UUID BANK_ACCOUNT_CAD =
        UUID.fromString("D9ACCC48-A0D9-4C33-AB13-38966B5BD8E2");
    private static final String BANK_ACCOUNT_CAD_NUMBER = "4440001000000000520";

    private static final UUID BANK_ACCOUNT_GBP =
        UUID.fromString("1944791A-0B92-4F83-815D-2C73C99F7980");
    private static final String BANK_ACCOUNT_GBP_NUMBER = "4440001000000000620";

    private static final UUID ACCOUNT_JOHN_DOO =
        UUID.fromString("5366EBCB-3F1C-4764-9E34-D2C61AA7E72A");
    private static final String ACCOUNT_JOHN_DOO_NUMBER = "4440001000000000512";

    private static final UUID ACCOUNT_JANE_STANDARD =
        UUID.fromString("04BFDE45-B5C6-433F-99D9-6485E55C28D6");
    private static final String ACCOUNT_JANE_STANDARD_NUMBER = "4440001000000000521";

    private static final UUID JOHN_CONTACT =
        UUID.fromString("25A92DCD-A4CE-4AFA-BDF7-37B391E27B58");
    private static final UUID JANE_CONTACT =
        UUID.fromString("8A2DD617-9EB3-4216-AFB0-26C54CB8DDA9");


    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrencyRepository currencyRepository;
    private final AccountRepository accountRepository;
    private final ActivityCodeRepository activityCodeRepository;
    private final CompanyRepository companyRepository;
    private final ClientContactRepository clientContactRepository;
    private final BankMarginRepository bankMarginRepository;
    private final InterestRateRepository interestRateRepository;
    private final CardRepository cardRepository;

    @Override
    public void run(String... args) {
        /* Production seeders. */
        interestRateSeeder();
        seedBankMargins();
        currencySeeder();
        activityCodeSeeder();
        bankSelfClientSeeder();
        bankSeeder();
        seedStateCompany();

        /* Dev-only seeders. */
        if (environment.matchesProfiles("dev")) {
            LOGGER.info("Inserting fake data (profiles includes 'dev')");
            employeeSeeder();
            clientSeeder();
            clientContactsSeeder();
            companySeeder();
            accountSeeder();
            cardSeeder();
            authorizedUserSeeder();
        }
    }

    /**
     * Install a client with ID {@link #CLIENT_BANK_SELF} that's used as the owner for all
     * bank-owned bank accounts.
     *
     * Disabled by default, as it lacks a password.
     */
    private void bankSelfClientSeeder() {
        var bankSelfClient =
            Client.builder()
                .id(CLIENT_BANK_SELF)
                .firstName("RAFeisen")
                .lastName("Bank")
                .dateOfBirth(LocalDate.of(2025, 2, 10))
                .gender(Gender.MALE)
                .email("admin@bankcorp.com")
                .password(passwordEncoder.encode("password"))
                .phone("+381651231231")
                .address("Mali Kalemegdan 8, Belgrade")
                .enabled(true)
                .build();

        clientRepository.saveAndFlush(bankSelfClient);
    }

    private void accountSeeder() {
        Employee employee =
            employeeRepository.findById(EMPLOYEE_ALICE)
                .orElseThrow(() -> new RuntimeException("Employee Alice not found"));

        Client clientJohn =
            clientRepository.findById(CLIENT_JOHN)
                .orElseThrow(() -> new RuntimeException("Client John not found"));

        Client clientJane =
            clientRepository.findById(CLIENT_JANE)
                .orElseThrow(() -> new RuntimeException("Client John not found"));

        Currency currencyRSD = currencyRepository.findByCode(CurrencyCode.Code.RSD);
        Currency currencyEUR = currencyRepository.findByCode(CurrencyCode.Code.EUR);

        Company company =
            companyRepository.findById(COMPANY_BIG_COMPANY_DOO)
                .orElseThrow(() -> new RuntimeException("Company BigCompany not found"));

        Account dooAcount =
            Account.builder()
                .id(ACCOUNT_JOHN_DOO)
                .accountNumber(ACCOUNT_JOHN_DOO_NUMBER)
                .balance(new BigDecimal("1000.00"))
                .availableBalance(new BigDecimal("1000.00"))
                .accountMaintenance(new BigDecimal("10.00"))
                .createdDate(LocalDate.now())
                .expirationDate(
                    LocalDate.now()
                        .plusYears(5)
                )
                .active(true)
                .accountType(AccountType.DOO)
                .dailyLimit(new BigDecimal("500.00"))
                .monthlyLimit(new BigDecimal("5000.00"))
                .client(clientJohn)
                .employee(employee)
                .currency(currencyRSD)
                .company(company)
                .build();

        Account standardAccount =
            Account.builder()
                .id(ACCOUNT_JANE_STANDARD)
                .accountNumber(ACCOUNT_JANE_STANDARD_NUMBER)
                .balance(new BigDecimal("1000.00"))
                .availableBalance(new BigDecimal("1000.00"))
                .accountMaintenance(new BigDecimal("10.00"))
                .createdDate(LocalDate.now())
                .expirationDate(
                    LocalDate.now()
                        .plusYears(5)
                )
                .active(true)
                .accountType(AccountType.STANDARD)
                .dailyLimit(new BigDecimal("500.00"))
                .monthlyLimit(new BigDecimal("5000.00"))
                .client(clientJane)
                .employee(employee)
                .currency(currencyEUR)
                .build();

        accountRepository.saveAndFlush(dooAcount);
        accountRepository.saveAndFlush(standardAccount);
    }

    private void companySeeder() {
        Client client =
            clientRepository.findById(CLIENT_JOHN)
                .orElseThrow(() -> new RuntimeException("Client John not found"));

        ActivityCode activityCode =
            activityCodeRepository.findActivityCodeByCode("62.01")
                .orElseThrow(() -> new RuntimeException("Activity code not found"));

        Company company =
            Company.builder()
                .id(COMPANY_BIG_COMPANY_DOO)
                .name("BigCompanyDOO")
                .tin("123456789")
                .crn("987654321")
                .address("789 Oak St")
                .activityCode(activityCode)
                .majorityOwner(client)
                .build();

        companyRepository.saveAndFlush(company);
    }

    private void employeeSeeder() {
        List<Employee> employees =
            List.of(
                Employee.builder()
                    .id(EMPLOYEE_ALICE)
                    .firstName("Alice")
                    .lastName("Johnson")
                    .dateOfBirth(LocalDate.of(1985, 4, 12))
                    .gender(Gender.FEMALE)
                    .email("alice.johnson@bankcorp.com")
                    .phone("+38166798580")
                    .address("789 Sunset Blvd")
                    .password(passwordEncoder.encode("password"))
                    .username("alicej")
                    .position("Branch Manager")
                    .department("Branch Operations")
                    .active(true)
                    .enabled(true)
                    .build(),

                Employee.builder()
                    .id(EMPLOYEE_ROBERT)
                    .firstName("Robert")
                    .lastName("Anderson")
                    .dateOfBirth(LocalDate.of(1990, 7, 21))
                    .gender(Gender.MALE)
                    .email("robert.anderson@bankcorp.com")
                    .phone("+38166798500")
                    .address("456 Greenway Rd")
                    .password(passwordEncoder.encode("password"))
                    .username("roberta")
                    .position("Loan Officer")
                    .department("Loans & Mortgages")
                    .active(true)
                    .enabled(true)
                    .build(),

                Employee.builder()
                    .id(EMPLOYEE_SAMANTHA)
                    .firstName("Samantha")
                    .lastName("Miller")
                    .dateOfBirth(LocalDate.of(1988, 10, 5))
                    .gender(Gender.FEMALE)
                    .email("samantha.miller@bankcorp.com")
                    .phone("+38166798511")
                    .address("321 Oak Dr")
                    .password(passwordEncoder.encode("password"))
                    .username("samantham")
                    .position("Financial Analyst")
                    .department("Finance")
                    .active(true)
                    .enabled(true)
                    .build(),

                Employee.builder()
                    .id(EMPLOYEE_DANIEL)
                    .firstName("Daniel")
                    .lastName("White")
                    .dateOfBirth(LocalDate.of(1993, 3, 15))
                    .gender(Gender.MALE)
                    .email("daniel.white@bankcorp.com")
                    .phone("+38166798522")
                    .address("567 Pine Ln")
                    .password(passwordEncoder.encode("password"))
                    .username("danielw")
                    .position("Investment Banker")
                    .department("Investment Banking")
                    .active(true)
                    .enabled(true)
                    .build(),

                Employee.builder()
                    .id(EMPLOYEE_JESSICA)
                    .firstName("Jessica")
                    .lastName("Martinez")
                    .dateOfBirth(LocalDate.of(1991, 5, 30))
                    .gender(Gender.FEMALE)
                    .email("jessica.martinez@bankcorp.com")
                    .phone("+38166798333")
                    .address("123 Cedar Ave")
                    .password(passwordEncoder.encode("password"))
                    .username("jessicam")
                    .position("Compliance Officer")
                    .department("Regulatory Compliance")
                    .active(true)
                    .enabled(true)
                    .build(),

                Employee.builder()
                    .id(EMPLOYEE_MICHAEL)
                    .firstName("Michael")
                    .lastName("Thompson")
                    .dateOfBirth(LocalDate.of(1987, 8, 18))
                    .gender(Gender.MALE)
                    .email("michael.thompson@bankcorp.com")
                    .phone("+38166798366")
                    .address("456 Maple Rd")
                    .password(passwordEncoder.encode("password"))
                    .username("michaelt")
                    .position("Risk Manager")
                    .department("Risk Management")
                    .active(true)
                    .enabled(true)
                    .build(),

                Employee.builder()
                    .id(EMPLOYEE_LAURA)
                    .firstName("Laura")
                    .lastName("Harris")
                    .dateOfBirth(LocalDate.of(1994, 12, 22))
                    .gender(Gender.FEMALE)
                    .email("laura.harris@bankcorp.com")
                    .phone("+38166798377")
                    .address("789 Elm St")
                    .password(passwordEncoder.encode("password"))
                    .username("laurah")
                    .position("Customer Service Representative")
                    .department("Customer Support")
                    .active(true)
                    .enabled(true)
                    .build(),

                Employee.builder()
                    .id(EMPLOYEE_DAVID)
                    .firstName("David")
                    .lastName("Clark")
                    .dateOfBirth(LocalDate.of(1989, 6, 9))
                    .gender(Gender.MALE)
                    .email("david.clark@bankcorp.com")
                    .phone("+38166798388")
                    .address("987 Walnut St")
                    .password(passwordEncoder.encode("password"))
                    .username("davidc")
                    .position("Credit Analyst")
                    .department("Credit & Lending")
                    .active(true)
                    .enabled(true)
                    .build(),

                Employee.builder()
                    .id(EMPLOYEE_EMMA)
                    .firstName("Emma")
                    .lastName("Lewis")
                    .dateOfBirth(LocalDate.of(1996, 2, 14))
                    .gender(Gender.FEMALE)
                    .email("emma.lewis@bankcorp.com")
                    .phone("+38166798399")
                    .address("654 Birch Rd")
                    .password(passwordEncoder.encode("password"))
                    .username("emmal")
                    .position("Treasury Analyst")
                    .department("Treasury")
                    .active(true)
                    .enabled(true)
                    .build(),

                Employee.builder()
                    .id(EMPLOYEE_CHRIS)
                    .firstName("Chris")
                    .lastName("Walker")
                    .dateOfBirth(LocalDate.of(1986, 11, 3))
                    .gender(Gender.MALE)
                    .email("chris.walker@bankcorp.com")
                    .phone("+38166798400")
                    .address("321 Redwood Ave")
                    .password(passwordEncoder.encode("password"))
                    .username("chrisw")
                    .position("Bank Teller")
                    .department("Retail Banking")
                    .active(true)
                    .enabled(true)
                    .build()
            );

        employees.forEach(employee -> {
            switch (employee.getPosition()) {
            case "Branch Manager",
                "Investment Banker"
                -> employee.setPrivileges(List.of(Privilege.ADMIN));
            case "Compliance Officer",
                "Risk Manager"
                -> employee.setPrivileges(List.of());
            case "Loan Officer",
                "Credit Analyst"
                -> employee.setPrivileges(List.of());
            default -> employee.setPrivileges(List.of());
            }
        });

        employeeRepository.saveAllAndFlush(employees);
    }

    private void clientSeeder() {
        List<Client> clients =
            List.of(
                Client.builder()
                    .id(CLIENT_JOHN)
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.of(1995, 5, 15))
                    .gender(Gender.MALE)
                    .email("johndoe95@example.com")
                    .phone("+38162798588")
                    .address("123 Main St")
                    .password(passwordEncoder.encode("password"))
                    .enabled(true)
                    .build(),
                Client.builder()
                    .id(CLIENT_JANE)
                    .firstName("Jane")
                    .lastName("Smith")
                    .dateOfBirth(LocalDate.of(1992, 8, 25))
                    .gender(Gender.FEMALE)
                    .email("janesmith92@example.com")
                    .phone("+38162798580")
                    .address("789 Oak St")
                    .password(passwordEncoder.encode("password"))
                    .enabled(true)
                    .build(),
                Client.builder()
                    .id(CLIENT_DANIEL)
                    .firstName("Daniel")
                    .lastName("Miller")
                    .dateOfBirth(LocalDate.of(1994, 7, 22))
                    .gender(Gender.MALE)
                    .email("danielm@example.com")
                    .phone("+38162372273")
                    .address("741 Redwood St")
                    .password(passwordEncoder.encode("password"))
                    .enabled(true)
                    .build()
            );

        clientRepository.saveAllAndFlush(clients);
    }

    private void cardSeeder() {
        List<Account> accounts = accountRepository.findAll();

        Account standardAccount =
            accounts.stream()
                .filter(account -> account.getAccountType() == AccountType.STANDARD)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No standard account found"));

        Account dooAccount =
            accounts.stream()
                .filter(account -> account.getAccountType() == AccountType.DOO)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No DOO account found"));

        List<Card> cards =
            List.of(
                Card.builder()
                    .id(CARD_STANDARD)
                    .cardNumber(CARD_STANDARD_NUMBER)
                    .cvv(CARD_STANDARD_CVV)
                    .cardName(CardName.VISA)
                    .cardType(CardType.DEBIT)
                    .account(standardAccount)
                    .cardStatus(CardStatus.ACTIVATED)
                    .limit(BigDecimal.valueOf(10000))
                    .createdAt(LocalDate.now())
                    .expiresAt(
                        LocalDate.now()
                            .plusYears(5)
                    )
                    .build(),

                Card.builder()
                    .id(CARD_DOO)
                    .cardNumber(CARD_DOO_NUMBER)
                    .cvv(CARD_DOO_CVV)
                    .cardName(CardName.MASTER_CARD)
                    .cardType(CardType.DEBIT)
                    .account(dooAccount)
                    .cardStatus(CardStatus.ACTIVATED)
                    .limit(BigDecimal.valueOf(10000))
                    .createdAt(LocalDate.now())
                    .expiresAt(
                        LocalDate.now()
                            .plusYears(5)
                    )
                    .build()
            );

        cardRepository.saveAllAndFlush(cards);
    }

    private void clientContactsSeeder() {
        Client clientJohn =
            clientRepository.findById(CLIENT_JOHN)
                .orElseThrow(() -> new RuntimeException("Client John not found"));

        Client clientJane =
            clientRepository.findById(CLIENT_JANE)
                .orElseThrow(() -> new RuntimeException("Client Jane not found"));

        ClientContact clientContactJohn =
            ClientContact.builder()
                .id(JOHN_CONTACT)
                .client(clientJohn)
                .accountNumber(ACCOUNT_JANE_STANDARD_NUMBER)
                .nickname(String.join(" ", clientJane.getFirstName(), clientJane.getLastName()))
                .build();

        ClientContact clientContactJane =
            ClientContact.builder()
                .id(JANE_CONTACT)
                .client(clientJane)
                .accountNumber(ACCOUNT_JOHN_DOO_NUMBER)
                .nickname(String.join(" ", clientJohn.getFirstName(), clientJohn.getLastName()))
                .build();

        clientContactRepository.saveAndFlush(clientContactJane);
        clientContactRepository.saveAndFlush(clientContactJohn);
    }

    private void activityCodeSeeder() {
        List<ActivityCode> activityCodes =
            List.of(
                ActivityCode.builder()
                    .code("1.11")
                    .sector("Poljoprivreda, šumarstvo i ribarstvo")
                    .branch("Uzgoj žitarica i mahunarki")
                    .build(),

                ActivityCode.builder()
                    .code("1.13")
                    .sector("Poljoprivreda, šumarstvo i ribarstvo")
                    .branch("Uzgoj povrća")
                    .build(),

                ActivityCode.builder()
                    .code("13.1")
                    .sector("Prerađivačka industrija")
                    .branch("Priprema i predenje tekstilnih vlakana")
                    .build(),

                ActivityCode.builder()
                    .code("24.1")
                    .sector("Prerađivačka industrija")
                    .branch("Proizvodnja gvožđa i čelika")
                    .build(),

                ActivityCode.builder()
                    .code("24.2")
                    .sector("Prerađivačka industrija")
                    .branch("Proizvodnja čeličnih cevi, šupljih profila i fitinga")
                    .build(),

                ActivityCode.builder()
                    .code("41.1")
                    .sector("Građevinarstvo")
                    .branch("Razvoj građevinskih projekata")
                    .build(),

                ActivityCode.builder()
                    .code("41.2")
                    .sector("Građevinarstvo")
                    .branch("Izgradnja stambenih i nestambenih zgrada")
                    .build(),

                ActivityCode.builder()
                    .code("42.11")
                    .sector("Građevinarstvo")
                    .branch("Izgradnja puteva i autoputeva")
                    .build(),

                ActivityCode.builder()
                    .code("42.12")
                    .sector("Građevinarstvo")
                    .branch("Izgradnja železničkih i podzemnih pruga")
                    .build(),

                ActivityCode.builder()
                    .code("42.13")
                    .sector("Građevinarstvo")
                    .branch("Izgradnja mostova i tunela")
                    .build(),

                ActivityCode.builder()
                    .code("42.21")
                    .sector("Građevinarstvo")
                    .branch("Izgradnja vodovodnih projekata")
                    .build(),

                ActivityCode.builder()
                    .code("42.22")
                    .sector("Građevinarstvo")
                    .branch("Izgradnja elektroenergetskih i telekomunikacionih mreža")
                    .build(),

                ActivityCode.builder()
                    .code("5.1")
                    .sector("Rudarstvo")
                    .branch("Vađenje uglja")
                    .build(),

                ActivityCode.builder()
                    .code("7.1")
                    .sector("Rudarstvo")
                    .branch("Vađenje gvozdenih ruda")
                    .build(),

                ActivityCode.builder()
                    .code("7.21")
                    .sector("Rudarstvo")
                    .branch("Vađenje uranijuma i torijuma")
                    .build(),

                ActivityCode.builder()
                    .code("8.11")
                    .sector("Rudarstvo")
                    .branch("Eksploatacija ukrasnog i građevinskog kamena")
                    .build(),

                ActivityCode.builder()
                    .code("8.92")
                    .sector("Rudarstvo")
                    .branch("Ekstrakcija treseta")
                    .build(),

                ActivityCode.builder()
                    .code("47.11")
                    .sector("Trgovina")
                    .branch("Trgovina u nespecijalizovanim prodavnicama sa hranom i pićem")
                    .build(),

                ActivityCode.builder()
                    .code("56.1")
                    .sector("Ugostiteljstvo")
                    .branch("Restorani i pokretni ugostiteljski objekti")
                    .build(),

                ActivityCode.builder()
                    .code("62.01")
                    .sector("IT")
                    .branch("Računarsko programiranje")
                    .build(),

                ActivityCode.builder()
                    .code("62.09")
                    .sector("IT")
                    .branch("Ostale IT usluge")
                    .build(),

                ActivityCode.builder()
                    .code("63.11")
                    .sector("IT")
                    .branch("Obrada podataka, hosting i slične delatnosti")
                    .build(),

                ActivityCode.builder()
                    .code("64.19")
                    .sector("Finansijske delatnosti")
                    .branch("Ostale monetarne posredničke delatnosti")
                    .build(),

                ActivityCode.builder()
                    .code("64.91")
                    .sector("Finansijske delatnosti")
                    .branch("Finansijski lizing")
                    .build(),

                ActivityCode.builder()
                    .code("64.2")
                    .sector("Finansijske delatnosti")
                    .branch("Holding kompanije")
                    .build(),

                ActivityCode.builder()
                    .code("66.3")
                    .sector("Finansijske delatnosti")
                    .branch("Fondovi i slične finansijske delatnosti")
                    .build(),

                ActivityCode.builder()
                    .code("65.2")
                    .sector("Osiguranje")
                    .branch("Reosiguranje")
                    .build(),

                ActivityCode.builder()
                    .code("65.11")
                    .sector("Osiguranje")
                    .branch("Životno osiguranje")
                    .build(),

                ActivityCode.builder()
                    .code("65.12")
                    .sector("Osiguranje")
                    .branch("Neživotno osiguranje")
                    .build(),

                ActivityCode.builder()
                    .code("66.21")
                    .sector("Osiguranje")
                    .branch("Procena rizika i štete")
                    .build(),

                ActivityCode.builder()
                    .code("68.1")
                    .sector("Poslovanje nekretninama")
                    .branch("Upravljanje nekretninama na osnovu naknade ili ugovora")
                    .build(),

                ActivityCode.builder()
                    .code("68.2")
                    .sector("Poslovanje nekretninama")
                    .branch(
                        "Izdavanje i upravljanje nekretninama u sopstvenom ili iznajmljenom vlasništvu"
                    )
                    .build(),

                ActivityCode.builder()
                    .code("53.1")
                    .sector("Saobraćaj i skladištenje")
                    .branch("Poštanske aktivnosti")
                    .build(),

                ActivityCode.builder()
                    .code("53.2")
                    .sector("Saobraćaj i skladištenje")
                    .branch("Kurirske aktivnosti")
                    .build(),

                ActivityCode.builder()
                    .code("85.1")
                    .sector("Obrazovanje")
                    .branch("Predškolsko obrazovanje")
                    .build(),

                ActivityCode.builder()
                    .code("85.2")
                    .sector("Obrazovanje")
                    .branch("Osnovno obrazovanje")
                    .build(),

                ActivityCode.builder()
                    .code("86.1")
                    .sector("Zdravstvena zaštita")
                    .branch("Bolničke aktivnosti")
                    .build(),

                ActivityCode.builder()
                    .code("86.21")
                    .sector("Zdravstvena zaštita")
                    .branch("Opšta medicinska praksa")
                    .build(),

                ActivityCode.builder()
                    .code("86.22")
                    .sector("Zdravstvena zaštita")
                    .branch("Specijalistička medicinska praksa")
                    .build(),

                ActivityCode.builder()
                    .code("86.9")
                    .sector("Zdravstvena zaštita")
                    .branch("Ostale aktivnosti zdravstvene zaštite")
                    .build(),

                ActivityCode.builder()
                    .code("84.12")
                    .sector("Javna uprava i odbrana")
                    .branch("Regulisanje delatnosti privrede")
                    .build(),

                ActivityCode.builder()
                    .code("90.01")
                    .sector("Kultura, sport i rekreacija")
                    .branch("Delatnost pozorišta")
                    .build(),

                ActivityCode.builder()
                    .code("90.02")
                    .sector("Kultura, sport i rekreacija")
                    .branch("Delatnost muzeja")
                    .build(),

                ActivityCode.builder()
                    .code("90.04")
                    .sector("Kultura, sport i rekreacija")
                    .branch("Delatnost botaničkih i zooloških vrtova")
                    .build(),

                ActivityCode.builder()
                    .code("93.11")
                    .sector("Sportske i rekreativne delatnosti")
                    .branch("Delovanje sportskih objekata")
                    .build(),

                ActivityCode.builder()
                    .code("93.13")
                    .sector("Sportske i rekreativne delatnosti")
                    .branch("Delovanje teretana")
                    .build(),

                ActivityCode.builder()
                    .code("93.19")
                    .sector("Sportske i rekreativne delatnosti")
                    .branch("Ostale sportske delatnosti")
                    .build(),

                ActivityCode.builder()
                    .code("26.11")
                    .sector("Proizvodnja elektronskih komponenti")
                    .branch("Proizvodnja elektronskih komponenti")
                    .build(),

                ActivityCode.builder()
                    .code("27.12")
                    .sector("Proizvodnja električne opreme")
                    .branch("Proizvodnja električnih panela i ploča")
                    .build(),

                ActivityCode.builder()
                    .code("29.1")
                    .sector("Proizvodnja motornih vozila")
                    .branch("Proizvodnja motornih vozila")
                    .build()
            );

        activityCodes.forEach(activityCode -> {
            if (!activityCodeRepository.existsByCode(activityCode.getCode()))
                activityCodeRepository.saveAndFlush(activityCode);
        });
    }

    protected void currencySeeder() {
        Set<Currency> currencies =
            Set.of(
                Currency.builder()
                    .name("Serbian Dinar")
                    .symbol("RSD")
                    .description("Serbian national currency")
                    .active(true)
                    .code(CurrencyCode.Code.RSD)
                    .build(),
                Currency.builder()
                    .name("Euro")
                    .symbol("EUR")
                    .description("European Union currency")
                    .active(true)
                    .code(CurrencyCode.Code.EUR)
                    .build(),
                Currency.builder()
                    .name("US Dollar")
                    .symbol("USD")
                    .description("United States currency")
                    .active(true)
                    .code(CurrencyCode.Code.USD)
                    .build(),
                Currency.builder()
                    .name("Swiss Franc")
                    .symbol("CHF")
                    .description("Swiss national currency")
                    .active(true)
                    .code(CurrencyCode.Code.CHF)
                    .build(),
                Currency.builder()
                    .name("Japanese Yen")
                    .symbol("JPY")
                    .description("Japanese national currency")
                    .active(true)
                    .code(CurrencyCode.Code.JPY)
                    .build(),
                Currency.builder()
                    .name("Australian Dollar")
                    .symbol("AUD")
                    .description("Australian national currency")
                    .active(true)
                    .code(CurrencyCode.Code.AUD)
                    .build(),
                Currency.builder()
                    .name("Canadian Dollar")
                    .symbol("CAD")
                    .description("Canadian national currency")
                    .active(true)
                    .code(CurrencyCode.Code.CAD)
                    .build(),
                Currency.builder()
                    .name("British Pound")
                    .symbol("GBP")
                    .description("United Kingdom national currency")
                    .active(true)
                    .code(CurrencyCode.Code.GBP)
                    .build()
            );

        // TODO(arsen): don't use findByCode. Actually, remove this whole thing. It is goofy.
        for (Currency currency : currencies) {
            if (currencyRepository.findByCode(currency.getCode()) == null) {
                currencyRepository.saveAndFlush(currency);
            }
        }
    }

    private void seedBankMargins() {
        bankMarginRepository.saveAndFlush(
            BankMargin.builder()
                .id(BANK_MARGIN_CASH)
                .type(LoanType.CASH)
                .margin(new BigDecimal("1.75"))
                .build()
        );

        bankMarginRepository.saveAndFlush(
            BankMargin.builder()
                .id(BANK_MARGIN_MORTGAGE)
                .type(LoanType.MORTGAGE)
                .margin(new BigDecimal("1.50"))
                .build()
        );

        bankMarginRepository.saveAndFlush(
            BankMargin.builder()
                .id(BANK_MARGIN_AUTO_LOAN)
                .type(LoanType.AUTO_LOAN)
                .margin(new BigDecimal("1.25"))
                .build()
        );

        bankMarginRepository.saveAndFlush(
            BankMargin.builder()
                .id(BANK_MARGIN_REFINANCING)
                .type(LoanType.REFINANCING)
                .margin(new BigDecimal("1.00"))
                .build()
        );

        bankMarginRepository.saveAndFlush(
            BankMargin.builder()
                .id(BANK_MARGIN_STUDENT_LOAN)
                .type(LoanType.STUDENT_LOAN)
                .margin(new BigDecimal("0.75"))
                .build()
        );
    }

    private void interestRateSeeder() {
        List<InterestRate> interestRates =
            List.of(
                createInterestRate(LOAN_INTEREST_0_500000, 0, 500000L, 6.25),
                createInterestRate(LOAN_INTEREST_500001_1000000, 500001, 1000000L, 6.00),
                createInterestRate(LOAN_INTEREST_1000001_2000000, 1000001, 2000000L, 5.75),
                createInterestRate(LOAN_INTEREST_2000001_5000000, 2000001, 5000000L, 5.50),
                createInterestRate(LOAN_INTEREST_5000001_10000000, 5000001, 10000000L, 5.25),
                createInterestRate(LOAN_INTEREST_10000001_20000000, 10000001, 20000000L, 5.00),
                createInterestRate(LOAN_INTEREST_20000001_2000000100, 20000001, 2000000100L, 4.75) // No
                                                                                                   // upper
                                                                                                   // limit
            );

        interestRateRepository.saveAllAndFlush(interestRates);
    }

    private InterestRate createInterestRate(
        UUID interestId,
        long minAmount,
        Long maxAmount,
        double fixedRate
    ) {
        return InterestRate.builder()
            .id(interestId)
            .minAmount(BigDecimal.valueOf(minAmount))
            .maxAmount(maxAmount != null ? BigDecimal.valueOf(maxAmount) : null)
            .fixedRate(BigDecimal.valueOf(fixedRate))
            .dateActiveFrom(LocalDate.now())
            .dateActiveTo(
                LocalDate.now()
                    .plusYears(1)
            )
            .build();
    }

    private void authorizedUserSeeder() {
        Card card =
            cardRepository.findById(CARD_DOO)
                .orElseThrow(() -> new RuntimeException("DOO Account not found"));

        AuthorizedUser authorizedUsers =
            AuthorizedUser.builder()
                .userId(CARD_DOO_AUTHORIZED_USER)
                .firstName("Mehmmedalija")
                .lastName("Karisik")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .email("random@example.com")
                .phoneNumber("+38166798076")
                .address("New Address 51")
                .gender(Gender.MALE)
                .build();

        card.setAuthorizedUser(authorizedUsers);
        cardRepository.saveAndFlush(card);
    }

    private void seedStateCompany() {
        Company stateCompany =
            Company.builder()
                .id(STATE_COMPANY)
                .name("The State")
                .tin("100100100")
                .crn("200200200")
                .address("Government Square, Belgrade")
                .build();

        companyRepository.saveAndFlush(stateCompany);

        Currency rsdCurrency = currencyRepository.findByCode(CurrencyCode.Code.RSD);

        Account stateAccount =
            Account.builder()
                .id(STATE_ACCOUNT)
                .accountNumber(STATE_ACCOUNT_NUMBER)
                .balance(BigDecimal.ZERO)
                .availableBalance(BigDecimal.ZERO)
                .accountMaintenance(BigDecimal.ZERO)
                .createdDate(LocalDate.now())
                .expirationDate(
                    LocalDate.now()
                        .plusYears(5)
                )
                .active(true)
                .accountType(AccountType.DOO)
                .company(stateCompany)
                .currency(rsdCurrency)
                .build();

        accountRepository.saveAndFlush(stateAccount);
    }

    private void bankSeeder() {
        ActivityCode activityCode =
            activityCodeRepository.findActivityCodeByCode("64.19")
                .orElseThrow(() -> new RuntimeException("ActivityCode not found"));

        Client client =
            clientRepository.findById(CLIENT_BANK_SELF)
                .orElseThrow(() -> new RuntimeException("Bank self client not found"));

        Company ourBank =
            Company.builder()
                .id(COMPANY_RAFFEISEN_BANK)
                .name("Raffeisen Bank")
                .tin("133456789")
                .crn("988654321")
                .address("Kneza Mihaila 6")
                .activityCode(activityCode)
                .majorityOwner(client)
                .build();

        companyRepository.saveAndFlush(ourBank);

        List<UUID> bankAccountIds =
            List.of(
                BANK_ACCOUNT_RSD,
                BANK_ACCOUNT_EUR,
                BANK_ACCOUNT_USD,
                BANK_ACCOUNT_CHF,
                BANK_ACCOUNT_JPY,
                BANK_ACCOUNT_AUD,
                BANK_ACCOUNT_CAD,
                BANK_ACCOUNT_GBP
            );

        List<String> bankAccountNumbers =
            List.of(
                BANK_ACCOUNT_RSD_NUMBER,
                BANK_ACCOUNT_EUR_NUMBER,
                BANK_ACCOUNT_USD_NUMBER,
                BANK_ACCOUNT_CHF_NUMBER,
                BANK_ACCOUNT_JPY_NUMBER,
                BANK_ACCOUNT_AUD_NUMBER,
                BANK_ACCOUNT_CAD_NUMBER,
                BANK_ACCOUNT_GBP_NUMBER
            );

        List<Currency> currencies = currencyRepository.findAll();
        List<Account> accounts = new ArrayList<>();

        for (int i = 0; i < bankAccountNumbers.size(); i++) {
            UUID accountId = bankAccountIds.get(i);
            String accountNumber = bankAccountNumbers.get(i);
            Currency currency = currencies.get(i);

            Account account =
                Account.builder()
                    .id(accountId)
                    .accountNumber(accountNumber)
                    .balance(BigDecimal.valueOf(1000000))
                    .availableBalance(BigDecimal.valueOf(1000000))
                    .accountMaintenance(BigDecimal.valueOf(100))
                    .createdDate(LocalDate.now())
                    .expirationDate(
                        LocalDate.now()
                            .plusYears(5)
                    )
                    .active(true)
                    .accountType(AccountType.DOO)
                    .dailyLimit(BigDecimal.valueOf(100000))
                    .monthlyLimit(BigDecimal.valueOf(1000000))
                    .company(ourBank)
                    .currency(currency)
                    .client(client)
                    .build();

            accounts.add(account);
        }

        accountRepository.saveAllAndFlush(accounts);
    }

}
