package rs.banka4.bank_service.domain.trading.db.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;
import rs.banka4.bank_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;

@Schema(
    description = "Data Transfer Object representing an OTC request (negotiation) for trading securities"
)
public record OtcRequestCreateDto(
    @Schema(
        description = "id of asset owner",
        example = "1fad2c01-f82f-41a6-822c-8ca1b3232575"
    ) @NotNull ForeignBankId userId,
    @Schema(
        description = "id of asset",
        example = "1fad2c01-f82f-41a6-822c-8ca1b3232575"
    ) @NotNull UUID assetId,
    @Schema(
        description = "The price per individual security unit",
        example = "150.00"
    ) @NotNull MonetaryAmount pricePerStock,

    @Schema(
        description = "The premium amount for the optional contract (opcionog ugovora)",
        example = "400.00"
    ) @NotNull MonetaryAmount premium,

    @Schema(
        description = "The amount (number) of securities being negotiated",
        example = "10"
    ) @NotNull int amount,

    @Schema(
        description = "The settlement date for the OTC request which determines the expiry date of the optional contract",
        example = "2025-05-22"
    ) @NotNull LocalDate settlementDate

) {
}
