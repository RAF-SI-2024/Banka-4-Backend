package rs.banka4.stock_service.service.mock.generators;

import java.math.BigDecimal;
import java.util.UUID;
import rs.banka4.stock_service.domain.security.forex.db.CurrencyCode;
import rs.banka4.stock_service.domain.security.forex.db.ForexLiquidity;
import rs.banka4.stock_service.domain.security.forex.dtos.ForexPairDto;

public class ForexPairObjectMother {
    public static ForexPairDto generateForexPairDto() {
        return new ForexPairDto(
            UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
            CurrencyCode.USD,
            CurrencyCode.EUR,
            ForexLiquidity.HIGH,
            new BigDecimal("1.2345")
        );
    }
}
