package rs.banka4.stock_service.domain.exchanges.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;
import rs.banka4.stock_service.domain.security.forex.db.CurrencyCode;

public record ExchangeDto(
    @Schema(
        description = "Exchange ID",
        example = "123e4567-e89b-12d3-a456-426614174000"
    ) UUID id,

    @Schema(
        description = "Name of the exchange",
        example = "New York Stock Exchange"
    ) String exchangeName,

    @Schema(
        description = "Acronym of the exchange",
        example = "NYSE"
    ) String exchangeAcronym,

    @Schema(
        description = "Market Identifier Code (MIC) of the exchange",
        example = "XNYS"
    ) String exchangeMICCode,

    @Schema(
        description = "Country or polity where the exchange is located",
        example = "United States"
    ) String polity,

    @Schema(
        description = "Time zone of the exchange",
        example = "America/New_York"
    ) String timeZone,

    @Schema(
        description = "Opening time of the exchange",
        example = "09:30"
    ) OffsetDateTime openTime,

    @Schema(
        description = "Closing time of the exchange",
        example = "16:00"
    ) OffsetDateTime closeTime,

    @Schema(
        description = "Currency used in the exchange",
        example = "USD"
    ) CurrencyCode currency
) {
}
