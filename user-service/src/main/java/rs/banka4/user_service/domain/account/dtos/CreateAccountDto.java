package rs.banka4.user_service.domain.account.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import javax.annotation.Nullable;
import rs.banka4.rafeisen.common.currency.CurrencyCode;
import rs.banka4.user_service.domain.company.dtos.CompanyDto;

@Schema(description = "Data Transfer Object representing an account")
public record CreateAccountDto(

    @Schema(description = "Client associated with the account") AccountClientIdDto client,

    @Nullable @Schema(description = "Company associated with the account") CompanyDto company,

    @NotNull(message = "Balance cannot be null")
    @Schema(
        description = "Initial balance",
        example = "1000.00"
    ) BigDecimal availableBalance,

    @NotNull(message = "Currency ID cannot be null")
    @Schema(
        description = "Currency ID associated with this account",
        example = "RSD"
    ) CurrencyCode.Code currency,

    @Schema(description = "Flag to indicate if a card should be created")
    @NotNull Boolean createCard
) {
}
