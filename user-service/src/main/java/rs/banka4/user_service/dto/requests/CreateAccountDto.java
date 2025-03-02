package rs.banka4.user_service.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import rs.banka4.user_service.models.AccountType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Data Transfer Object representing an account")
public record CreateAccountDto (
        @NotNull(message = "Balance cannot be null")
        @Schema(description = "Initial balance", example = "1000.00")
        BigDecimal balance,

        @NotNull(message = "Expiration date cannot be null")
        @Schema(description = "Expiration date", example = "2024-01-01")
        LocalDate expirationDate,

        @NotNull(message = "Active status cannot be null")
        @Schema(description = "Active status", example = "true")
        Boolean active,

        @NotNull(message = "Account type cannot be null")
        @Schema(description = "Type of account", example = "SAVINGS")
        AccountType accountType,

        @NotNull(message = "Daily limit cannot be null")
        @Schema(description = "Daily limit", example = "100.00")
        BigDecimal dailyLimit,

        @NotNull(message = "Monthly limit cannot be null")
        @Schema(description = "Monthly limit", example = "1000.00")
        BigDecimal monthlyLimit,

        @NotNull(message = "Daily spending cannot be null")
        @Schema(description = "Daily spending", example = "50.00")
        BigDecimal dailySpending,

        @NotNull(message = "Monthly spending cannot be null")
        @Schema(description = "Monthly spending", example = "200.00")
        BigDecimal monthlySpending,

        @NotNull(message = "Currency ID cannot be null")
        @Schema(description = "Currency ID associated with this account", example = "11111111-2222-3333-4444-555555555555")
        UUID currencyId

) { }
