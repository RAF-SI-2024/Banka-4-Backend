package rs.banka4.user_service.domain.account.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import rs.banka4.user_service.domain.currency.dtos.CurrencyDto;
import rs.banka4.user_service.domain.user.client.dtos.ClientDto;
import rs.banka4.user_service.domain.user.employee.dtos.EmployeeDto;
import rs.banka4.user_service.domain.company.dtos.CompanyDto;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "DTO for account details")
public record AccountDto(
        @Schema(description = "Account ID", example = "11111111-2222-3333-4444-555555555555")
        String id,

        @Schema(description = "Account number", example = "1234567890")
        String accountNumber,

        @Schema(description = "Current balance", example = "1000.00")
        BigDecimal balance,

        @Schema(description = "Available balance", example = "800.00")
        BigDecimal availableBalance,

        @Schema(description = "Account maintenance fee", example = "100.00")
        BigDecimal accountMaintenance,

        @Schema(description = "Created date", example = "2023-01-01")
        LocalDate createdDate,

        @Schema(description = "Expiration date", example = "2028-01-01")
        LocalDate expirationDate,

        @Schema(description = "Active status", example = "true")
        boolean active,

        @Schema(description = "Type of account", example = "CheckingPersonal")
        AccountTypeDto accountType,

        @Schema(description = "Daily limit", example = "100.00")
        BigDecimal dailyLimit,

        @Schema(description = "Monthly limit", example = "1000.00")
        BigDecimal monthlyLimit,

        @Schema(description = "Currency associated with the account")
        CurrencyDto currency,

        @Schema(description = "Employee that created this account")
        EmployeeDto employee,

        @Nullable
        @Schema(description = "Client associated with the account")
        ClientDto client,

        @Nullable
        @Schema(description = "Company associated with the account")
        CompanyDto company
) { }
