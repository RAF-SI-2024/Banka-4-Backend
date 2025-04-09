package rs.banka4.user_service.domain.user.employee.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import rs.banka4.rafeisen.common.currency.CurrencyCode;

@Schema(description = "Payload for actuary-specific information")
public record ActuaryPayloadDto(

    @Schema(
        description = "Indicates whether the actuary needs approval for operations",
        example = "true"
    ) @NotNull(message = "needsApproval is required") boolean needsApproval,

    @Schema(
        description = "Limit amount assigned to the actuary",
        example = "10000"
    ) BigDecimal limitAmount,

    @Schema(
        description = "Currency code for the limit",
        example = "RSD"
    ) @NotNull(message = "Limit currency code is required") CurrencyCode limitCurrencyCode,

    @Schema(
        description = "ID of the actuary (user)",
        example = "6d03b02b-b2d7-4de6-b2d5-9917f44d2f5a"
    ) @NotNull(message = "Actuary ID is required") UUID actuaryId

) {
}
