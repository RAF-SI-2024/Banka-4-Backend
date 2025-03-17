package rs.banka4.user_service.domain.account.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record SetAccountLimitsDto(
        @PositiveOrZero(message = "Daily limit must be positive or zero")
        BigDecimal daily,

        @PositiveOrZero(message = "Monthly limit must be positive or zero")
        BigDecimal monthly,

        @NotBlank(message = "TOTP code content is required")
        @NotNull(message = "TOTP code cannot be null")
        String otpCode
) {}