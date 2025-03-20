package rs.banka4.stock_service.service.mock.generators;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import rs.banka4.stock_service.domain.exchanges.dtos.ExchangeDto;
import rs.banka4.stock_service.domain.security.forex.db.CurrencyCode;

public class ExchangeObjectMother {
    public static ExchangeDto generateExchangeDto() {
        return new ExchangeDto(
            UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
            "New York Stock Exchange",
            "NYSE",
            "XNYS",
            "United States",
            "America/New_York",
            OffsetDateTime.of(2023, 1, 1, 9, 30, 0, 0, ZoneOffset.of("-05:00")),
            OffsetDateTime.of(2023, 1, 1, 16, 0, 0, 0, ZoneOffset.of("-05:00")),
            CurrencyCode.USD
        );
    }

    public static ExchangeDto generateNYSEExchangeDto() {
        return new ExchangeDto(
            UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
            "New York Stock Exchange",
            "NYSE",
            "XNYS",
            "United States",
            "America/New_York",
            OffsetDateTime.of(2023, 1, 1, 9, 30, 0, 0, ZoneOffset.of("-05:00")),
            OffsetDateTime.of(2023, 1, 1, 16, 0, 0, 0, ZoneOffset.of("-05:00")),
            CurrencyCode.USD
        );
    }

    public static ExchangeDto generateLondonExchangeDto() {
        return new ExchangeDto(
            UUID.fromString("223e4567-e89b-12d3-a456-426614174001"),
            "London Stock Exchange",
            "LSE",
            "XLON",
            "United Kingdom",
            "Europe/London",
            OffsetDateTime.of(2023, 1, 1, 9, 30, 0, 0, ZoneOffset.of("-05:00")),
            OffsetDateTime.of(2023, 1, 1, 16, 0, 0, 0, ZoneOffset.of("-05:00")),
            CurrencyCode.USD
        );
    }

    public static ExchangeDto generateTokyoExchangeDto() {
        return new ExchangeDto(
            UUID.fromString("323e4567-e89b-12d3-a456-426614174002"),
            "Tokyo Stock Exchange",
            "TSE",
            "XTKS",
            "Japan",
            "Asia/Tokyo",
            OffsetDateTime.of(2023, 1, 1, 9, 30, 0, 0, ZoneOffset.of("-05:00")),
            OffsetDateTime.of(2023, 1, 1, 16, 0, 0, 0, ZoneOffset.of("-05:00")),
            CurrencyCode.USD
        );
    }

    public static ExchangeDto generateFrankfurtExchangeDto() {
        return new ExchangeDto(
            UUID.fromString("423e4567-e89b-12d3-a456-426614174003"),
            "Frankfurt Stock Exchange",
            "FSE",
            "XFRA",
            "Germany",
            "Europe/Berlin",
            OffsetDateTime.of(2023, 1, 1, 9, 30, 0, 0, ZoneOffset.of("-05:00")),
            OffsetDateTime.of(2023, 1, 1, 16, 0, 0, 0, ZoneOffset.of("-05:00")),
            CurrencyCode.USD
        );
    }
}
