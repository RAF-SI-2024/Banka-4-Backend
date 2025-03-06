package rs.banka4.user_service.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import rs.banka4.user_service.dto.ClientDto;
import rs.banka4.user_service.dto.CompanyDto;
import rs.banka4.user_service.models.AccountType;
import rs.banka4.user_service.models.Currency;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Data Transfer Object representing an account")
public record CreateAccountDto (

        @Schema(description = "Client associated with the account")
        CreateClientDto client,

        @Nullable
        @Schema(description = "Company associated with the account")
        CompanyDto company,

        @NotNull(message = "Balance cannot be null")
        @Schema(description = "Initial balance", example = "1000.00")
        BigDecimal availableBalance,

        @NotNull(message = "Currency ID cannot be null")
        @Schema(description = "Currency ID associated with this account", example = "RSD")
        Currency.Code currency
) { }
