package rs.banka4.stock_service.service.mock.generators;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import rs.banka4.stock_service.domain.listing.dtos.ListingDto;

public class ListingObjectMother {
    public static ListingDto generateListingDto() {
        return new ListingDto(
            UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
            null, // StockDto
            null, // ForexPairDto
            null, // FutureDto
            "AAPL",
            "Apple Inc.",
            ExchangeObjectMother.generateLondonExchangeDto(), // ExchangeDto
            OffsetDateTime.of(2023, 1, 1, 16, 0, 0, 0, ZoneOffset.of("-05:00")),
            new BigDecimal("150.50"),
            new BigDecimal("151.00")
        );
    }

    public static ListingDto generateStockListing() {
        return new ListingDto(
            UUID.fromString("523e4567-e89b-12d3-a456-426614174004"),
            StockObjectMother.generateStockDto(),
            null, // ForexPairDto
            null, // FutureDto
            "MSFT",
            "Microsoft Corporation",
            ExchangeObjectMother.generateExchangeDto(),
            OffsetDateTime.of(2023, 1, 1, 16, 0, 0, 0, ZoneOffset.of("-05:00")),
            new BigDecimal("250.75"),
            new BigDecimal("251.25")
        );
    }

    public static ListingDto generateForexListing() {
        return new ListingDto(
            UUID.fromString("623e4567-e89b-12d3-a456-426614174005"),
            null, // StockDto
            ForexPairObjectMother.generateForexPairDto(),
            null, // FutureDto
            "EUR/USD",
            "Euro to US Dollar",
            ExchangeObjectMother.generateFrankfurtExchangeDto(),
            OffsetDateTime.of(2023, 1, 1, 16, 0, 0, 0, ZoneOffset.of("-05:00")),
            new BigDecimal("1.1234"),
            new BigDecimal("1.1250")
        );
    }

    public static ListingDto generateFutureListing() {
        return new ListingDto(
            UUID.fromString("723e4567-e89b-12d3-a456-426614174006"),
            null, // StockDto
            null, // ForexPairDto
            FutureObjectMother.generateFutureDto(),
            "CORN-FUT",
            "Corn Futures",
            ExchangeObjectMother.generateTokyoExchangeDto(),
            OffsetDateTime.of(2023, 1, 1, 16, 0, 0, 0, ZoneOffset.of("-05:00")),
            new BigDecimal("75.00"),
            new BigDecimal("75.75")
        );
    }
}
