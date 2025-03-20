package rs.banka4.stock_service.service.mock.generators;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import rs.banka4.stock_service.domain.security.future.db.UnitName;
import rs.banka4.stock_service.domain.security.future.dtos.FutureDto;

public class FutureObjectMother {
    public static FutureDto generateFutureDto() {
        return new FutureDto(
            UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
            "Texas Corn",
            1000,
            UnitName.BARREL,
            OffsetDateTime.of(2023, 1, 1, 16, 0, 0, 0, ZoneOffset.of("-05:00")),
            new BigDecimal("75.50")
        );
    }
}
