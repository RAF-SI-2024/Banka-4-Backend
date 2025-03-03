package rs.banka4.user_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import rs.banka4.user_service.models.AccountType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "DTO for creating a new account")
public record CreateAccountDto(
        @Schema(description = "Account number", example = "1234567890")
        String accountNumber,

        @Schema(description = "Initial balance", example = "1000.00")
        BigDecimal balance,

        @Schema(description = "Available balance", example = "800.00")
        BigDecimal availableBalance,

        @Schema(description = "Created date", example = "2023-01-01")
        LocalDate createdDate,

        @Schema(description = "Expiration date", example = "2024-01-01")
        LocalDate expirationDate,

        @Schema(description = "Active status", example = "true")
        boolean active,

        @Schema(description = "Type of account", example = "SAVINGS")
        AccountType accountType,

        @Schema(description = "Daily limit", example = "100.00")
        BigDecimal dailyLimit,

        @Schema(description = "Monthly limit", example = "1000.00")
        BigDecimal monthlyLimit,

        @Schema(description = "Daily spending", example = "50.00")
        BigDecimal dailySpending,

        @Schema(description = "Monthly spending", example = "200.00")
        BigDecimal monthlySpending,

        @Schema(description = "Currency ID to be associated with this account", example = "11111111-2222-3333-4444-555555555555")
        UUID currencyId

) { }
