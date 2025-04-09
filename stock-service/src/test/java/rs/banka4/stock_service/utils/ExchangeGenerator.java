package rs.banka4.stock_service.utils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import rs.banka4.rafeisen.common.currency.CurrencyCode;
import rs.banka4.stock_service.domain.exchanges.db.Exchange;

public class ExchangeGenerator {
    private static final UUID EXCHANGE_BER1 =
        UUID.fromString("be8677a4-6e75-4645-af25-e31e8642be83");

    public static Exchange makeBer1() {
        return Exchange.builder()
            .id(EXCHANGE_BER1)
            .exchangeName("Nasdaq")
            .exchangeAcronym("NASDAQ")
            .exchangeMICCode("XNAS")
            .polity("USA")
            .currency(CurrencyCode.USD)
            .timeZone("Europe/Belgrade")
            .openTime(
                OffsetDateTime.of(LocalDate.now(), LocalTime.of(9, 30), ZoneOffset.of("+01:00"))
            )
            .closeTime(
                OffsetDateTime.of(LocalDate.now(), LocalTime.of(16, 0), ZoneOffset.of("+01:00"))
            )
            .createdAt(LocalDate.now())
            .build();
    }
}
