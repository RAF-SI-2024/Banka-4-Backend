package rs.banka4.stock_service.service.mock.generators;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import rs.banka4.stock_service.domain.security.stock.dtos.StockDto;

public class StockObjectMother {
    public static StockDto generateStockDto() {
        return new StockDto(
            UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
            "Microsoft Company",
            1000000,
            new BigDecimal("2.5"),
            OffsetDateTime.of(2023, 1, 1, 16, 0, 0, 0, ZoneOffset.of("-05:00")),
            new BigDecimal("150.75"),
            new BigDecimal("150750000.00")
        );
    }
}
