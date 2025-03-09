package rs.banka4.user_service.generator;

import rs.banka4.user_service.domain.account.dtos.AccountClientIdDto;
import rs.banka4.user_service.domain.account.dtos.AccountDto;
import rs.banka4.user_service.domain.account.dtos.CreateAccountDto;
import rs.banka4.user_service.domain.account.dtos.AccountTypeDto;
import rs.banka4.user_service.domain.currency.db.Currency;
import rs.banka4.user_service.domain.currency.dtos.CurrencyDto;
import rs.banka4.user_service.domain.user.Privilege;
import rs.banka4.user_service.domain.user.User;
import rs.banka4.user_service.domain.user.client.dtos.ClientDto;
import rs.banka4.user_service.domain.user.employee.dtos.EmployeeDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.UUID;

public class AccountObjectMother {

    public static CreateAccountDto generateBasicCreateAccountDto() {
        return new CreateAccountDto(
                new AccountClientIdDto(
                        UUID.randomUUID(),
                        "John",
                        "Doe",
                        LocalDate.of(1990, 1, 1),
                        "Male",
                        "john.doe@example.com",
                        "+1234567890",
                        "123 Grove Street, City, Country",
                        EnumSet.noneOf(Privilege.class)
                ),
                null,
                BigDecimal.valueOf(1000.00),
                Currency.Code.RSD
        );
    }

    public static AccountDto generateBasicAccountDto() {
        return new AccountDto(
                UUID.randomUUID().toString(),
                "1234567890",
                BigDecimal.valueOf(1000.00),
                BigDecimal.valueOf(800.00),
                BigDecimal.valueOf(100.00),
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2028, 1, 1),
                true,
                AccountTypeDto.CheckingPersonal,
                BigDecimal.valueOf(100.00),
                BigDecimal.valueOf(1000.00),
                new CurrencyDto(UUID.randomUUID(), "Serbian Dinar", "RSD", "Serbian Dinar currency", true, Currency.Code.RSD),
                new EmployeeDto(UUID.randomUUID(), "John", "Doe", LocalDate.of(1990, 1, 1), User.Gender.MALE, "mehmedalija.doe@example.com", "+1234567890", "123 Main St", "Mahd", "Developer", "IT",  true),
                new ClientDto(UUID.randomUUID(), "Jane", "Doe", LocalDate.of(1990, 1, 1), User.Gender.FEMALE, "jane.doe@example.com", "+1234567890", "123 Main St", EnumSet.noneOf(Privilege.class)),
                null
        );
    }
}