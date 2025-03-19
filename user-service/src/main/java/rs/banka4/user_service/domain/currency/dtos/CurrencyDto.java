package rs.banka4.user_service.domain.currency.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import rs.banka4.user_service.domain.currency.db.Currency.Code;

@Schema(description = "DTO for currency details")
public record CurrencyDto(

    @Schema(
        description = "Currency ID",
        example = "11111111-2222-3333-4444-555555555555"
    ) UUID id,

    @Schema(
        description = "Name",
        example = "Serbian Dinar"
    ) String name,

    @Schema(
        description = "Symbol",
        example = "RSD"
    ) String symbol,

    @Schema(
        description = "Description",
        example = "Serbian Dinar currency"
    ) String description,

    @Schema(
        description = "Active status",
        example = "true"
    ) boolean active,

    @Schema(
        description = "Currency code",
        example = "RSD"
    ) Code code
) {
}
