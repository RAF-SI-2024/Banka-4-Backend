package rs.banka4.user_service.generator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.UUID;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.account.db.AccountType;
import rs.banka4.user_service.domain.account.dtos.AccountClientIdDto;
import rs.banka4.user_service.domain.account.dtos.AccountDto;
import rs.banka4.user_service.domain.account.dtos.AccountTypeDto;
import rs.banka4.user_service.domain.account.dtos.CreateAccountDto;
import rs.banka4.user_service.domain.company.dtos.CompanyDto;
import rs.banka4.user_service.domain.currency.db.Currency;
import rs.banka4.user_service.domain.currency.db.Currency.Code;
import rs.banka4.user_service.domain.currency.mapper.CurrencyMapper;
import rs.banka4.user_service.domain.user.Gender;
import rs.banka4.user_service.domain.user.Privilege;
import rs.banka4.user_service.domain.user.client.dtos.ClientDto;
import rs.banka4.user_service.domain.user.employee.dtos.EmployeeDto;

public class AccountObjectMother {

    public static CreateAccountDto generateBasicCreateAccountDto() {
        return new CreateAccountDto(
            new AccountClientIdDto(
                UUID.randomUUID(),
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                Gender.MALE,
                "john.doe@example.com",
                "+1234567890",
                "123 Grove Street, City, Country",
                EnumSet.noneOf(Privilege.class)
            ),
            null,
            BigDecimal.valueOf(1000.00),
            Currency.Code.RSD,
            false
        );
    }

    public static CreateAccountDto generateBusinessAccount() {
        return new CreateAccountDto(
            new AccountClientIdDto(
                UUID.randomUUID(),
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                Gender.MALE,
                "john.doe@example.com",
                "+1234567890",
                "123 Grove Street, City, Country",
                EnumSet.noneOf(Privilege.class)
            ),
            new CompanyDto(
                "1231313131213123312",
                "Test Plumbing",
                "12312312",
                "12312313",
                "testAdresss",
                "testACode"
            ),
            BigDecimal.ZERO,
            Currency.Code.EUR,
            false
        );
    }

    public static AccountDto generateBasicAccountDto() {
        return new AccountDto(
            UUID.randomUUID()
                .toString(),
            "444394438340549",
            BigDecimal.valueOf(1000.00),
            BigDecimal.valueOf(800.00),
            BigDecimal.valueOf(100.00),
            LocalDate.of(2023, 1, 1),
            LocalDate.of(2028, 1, 1),
            true,
            AccountTypeDto.CheckingPersonal,
            BigDecimal.valueOf(100.00),
            BigDecimal.valueOf(1000.00),
            CurrencyMapper.INSTANCE.toDto(
                Currency.builder()
                    .code(Code.RSD)
                    .build()
            ),
            new EmployeeDto(
                UUID.randomUUID(),
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                Gender.MALE,
                "mehmedalija.doe@example.com",
                "+1234567890",
                "123 Main St",
                "Mahd",
                "Developer",
                "IT",
                true
            ),
            new ClientDto(
                UUID.randomUUID(),
                "Jane",
                "Doe",
                LocalDate.of(1990, 1, 1),
                Gender.FEMALE,
                "jane.doe@example.com",
                "+1234567890",
                "123 Main St",
                EnumSet.noneOf(Privilege.class),
                false
            ),
            null
        );
    }

    public static Account generateBasicFromAccount() {
        Account account = new Account();
        account.setAccountNumber("444394438340549");
        account.setBalance(BigDecimal.valueOf(10000.00));
        account.setAvailableBalance(BigDecimal.valueOf(8000.00));
        account.setActive(true);
        account.setAccountType(AccountType.STANDARD);
        account.setDailyLimit(BigDecimal.valueOf(1000.00));
        account.setMonthlyLimit(BigDecimal.valueOf(10000.00));
        account.setCurrency(
            new Currency(
                UUID.randomUUID(),
                1L,
                "Serbian Dinar",
                "RSD",
                "Serbian Dinar currency",
                true,
                Currency.Code.RSD
            )
        );
        account.setEmployee(EmployeeObjectMother.generateBasicEmployee());
        account.setClient(
            ClientObjectMother.generateClient(
                UUID.fromString("9df5e618-f21d-48a7-a7a4-ac55ea8bec97"),
                "markezaa@example.com"
            )
        );
        return account;
    }

    public static Account generateBasicEURFromAccount() {
        Account account = new Account();
        account.setAccountNumber("444394438340549");
        account.setBalance(BigDecimal.valueOf(10000.00));
        account.setAvailableBalance(BigDecimal.valueOf(8000.00));
        account.setActive(true);
        account.setAccountType(AccountType.STANDARD);
        account.setDailyLimit(BigDecimal.valueOf(1000.00));
        account.setMonthlyLimit(BigDecimal.valueOf(10000.00));
        account.setCurrency(
            new Currency(
                UUID.randomUUID(),
                1L,
                "European Currency",
                "EUR",
                "European currency",
                true,
                Currency.Code.EUR
            )
        );
        account.setEmployee(EmployeeObjectMother.generateBasicEmployee());
        account.setClient(
            ClientObjectMother.generateClient(
                UUID.fromString("9df5e618-f21d-48a7-a7a4-ac55ea8bec97"),
                "markezaa@example.com"
            )
        );
        return account;
    }

    public static Account generateBasicToAccount() {
        Account account = new Account();
        account.setAccountNumber("444394438340523");
        account.setBalance(BigDecimal.valueOf(10000.00));
        account.setAvailableBalance(BigDecimal.valueOf(8000.00));
        account.setActive(true);
        account.setAccountType(AccountType.STANDARD);
        account.setDailyLimit(BigDecimal.valueOf(1000.00));
        account.setMonthlyLimit(BigDecimal.valueOf(10000.00));
        account.setCurrency(
            new Currency(
                UUID.randomUUID(),
                1L,
                "Serbian Dinar",
                "RSD",
                "Serbian Dinar currency",
                true,
                Currency.Code.RSD
            )
        );
        account.setEmployee(EmployeeObjectMother.generateBasicEmployee());
        account.setClient(
            ClientObjectMother.generateClient(
                UUID.fromString("9df5e618-f21d-48a7-a7a4-ac55ea8bec93"),
                "zorz@example.com"
            )
        );
        return account;
    }

}
