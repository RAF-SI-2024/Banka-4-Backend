package rs.banka4.user_service.generator;

import rs.banka4.user_service.dto.*;
import rs.banka4.user_service.dto.requests.CreateClientDto;
import rs.banka4.user_service.dto.requests.CreateEmployeeDto;
import rs.banka4.user_service.models.AccountType;
import rs.banka4.user_service.models.Currency;
import rs.banka4.user_service.models.Privilege;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AccountObjectMother {

    public static AccountDto generateBasicAccountDto() {
        return new AccountDto(
                UUID.fromString("4fe1f3ce-25e2-4b1c-b1f0-bec874e96555"),
                "1234567890",
                BigDecimal.valueOf(1000.00),
                BigDecimal.valueOf(800.00),
                BigDecimal.valueOf(100.00),
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2028, 1, 1),
                true,
                generateAccountTypeDto(),
                BigDecimal.valueOf(100.00),
                BigDecimal.valueOf(1000.00),
                generateCurrencyDto(),
                generateEmployeeDto(),
                generateClientDto(),
                null
        );
    }

    public static AccountTypeDto generateAccountTypeDto() {
        return AccountTypeDto.CheckingBusiness;
    }

    public static CurrencyDto generateCurrencyDto() {
        return new CurrencyDto(
                UUID.randomUUID(),
                "United States Dollar",
                "USD",
                "Official currency of the United States",
                true,
                Currency.Code.USD,
                Set.of("United States", "Ecuador", "El Salvador")
        );
    }

    public static EmployeeDto generateEmployeeDto() {
        return new EmployeeDto(
                "1de54a3a-d879-4154-aa3a-e40598186f93", "John", "Doe", LocalDate.of(1990, 1, 1),
                "Male", "john.doe@example.com", "+1234567890", "123 Main St",
                "johndoe", "Developer", "IT", true);
    }

    public static ClientDto generateClientDto() {
        return new ClientDto(
                "John", "Doe", "johndoe", LocalDate.of(1990, 1, 1),
                "Male", "john.doe@example.com", "+1234567890", "123 Main St",
                EnumSet.of(Privilege.SEARCH), List.of());
    }

    public static CompanyDto generateCompanyDto() {
        return new CompanyDto(
                UUID.randomUUID(),
                "TechCorp Ltd.",
                "info@techcorp.com",
                "987-654-3210",
                "Kneza Mihaila 6/6"
        );
    }
}