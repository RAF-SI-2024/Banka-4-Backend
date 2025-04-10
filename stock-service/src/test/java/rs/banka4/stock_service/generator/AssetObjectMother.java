package rs.banka4.stock_service.generator;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import rs.banka4.stock_service.domain.security.stock.db.Stock;

public class AssetObjectMother {

    /**
     * Generates a basic Stock instance.
     *
     * @return a new Stock instance
     */
    public static Stock generateBasicStock() {
        return Stock.builder()
            .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
            .name("Apple Inc.")
            .ticker("AAPL")
            .outstandingShares(1000000000L)
            .dividendYield(BigDecimal.valueOf(0.005))
            .createdAt(OffsetDateTime.now())
            .build();
    }

}
