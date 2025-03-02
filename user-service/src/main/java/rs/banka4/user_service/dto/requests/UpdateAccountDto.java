package rs.banka4.user_service.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import rs.banka4.user_service.models.AccountType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "DTO for updating account details")
public record UpdateAccountDto(
        @NotBlank(message = "Account number is required and cannot be blank.")
        @Schema(description = "Account number", example = "1234567890")
        String accountNumber,

        @NotNull(message = "Balance is required and cannot be null.")
        @Schema(description = "Balance", example = "1000.00")
        BigDecimal balance,

        @NotNull(message = "Expiration date is required and cannot be null.")
        @Schema(description = "Expiration date", example = "2024-01-01")
        LocalDate expirationDate,

        @NotNull(message = "Active status is required and cannot be null.")
        @Schema(description = "Active status", example = "true")
        boolean active,

        @NotNull(message = "Account type is required and cannot be null.")
        @Schema(description = "Type of account", example = "SAVINGS")
        AccountType accountType,

        @NotNull(message = "Daily limit is required and cannot be null.")
        @Schema(description = "Daily limit", example = "100.00")
        BigDecimal dailyLimit,

        @NotNull(message = "Monthly limit is required and cannot be null.")
        @Schema(description = "Monthly limit", example = "1000.00")
        BigDecimal monthlyLimit,

        @NotNull(message = "Daily spending is required and cannot be null.")
        @Schema(description = "Daily spending", example = "50.00")
        BigDecimal dailySpending,

        @NotNull(message = "Monthly spending is required and cannot be null.")
        @Schema(description = "Monthly spending", example = "200.00")
        BigDecimal monthlySpending,

        @NotNull(message = "Currency ID is required and cannot be null.")
        @Schema(description = "Currency ID to be associated with this account", example = "11111111-2222-3333-4444-555555555555")
        UUID currencyId

) { }
