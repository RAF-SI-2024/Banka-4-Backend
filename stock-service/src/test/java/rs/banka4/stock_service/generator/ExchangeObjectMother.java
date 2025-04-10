package rs.banka4.stock_service.generator;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import rs.banka4.rafeisen.common.currency.CurrencyCode;
import rs.banka4.stock_service.domain.exchanges.db.Exchange;

public class ExchangeObjectMother {

    /**
     * Generates a basic Exchange instance.
     *
     * @return a new Exchange instance
     */
    public static Exchange generateBasicExchange() {
        return Exchange.builder()
            .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
            .exchangeName("New York Stock Exchange")
            .exchangeAcronym("NYSE")
            .exchangeMICCode("XNYS")
            .polity("USA")
            .currency(CurrencyCode.USD)
            .timeZone("America/New_York")
            .openTime(OffsetDateTime.of(2023, 1, 1, 9, 30, 0, 0, ZoneOffset.of("-05:00")))
            .closeTime(OffsetDateTime.of(2023, 1, 1, 16, 0, 0, 0, ZoneOffset.of("-05:00")))
            .build();
    }

}
