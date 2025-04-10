package rs.banka4.stock_service.generator;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import rs.banka4.stock_service.domain.exchanges.db.Exchange;
import rs.banka4.stock_service.domain.listing.db.Listing;
import rs.banka4.stock_service.domain.security.Security;

public class ListingObjectMother {

    /**
     * Generates a basic Listing instance.
     *
     * @return a new Listing instance
     */
    public static Listing generateBasicListing() {
        Security security = AssetObjectMother.generateBasicStock();
        Exchange exchange = ExchangeObjectMother.generateBasicExchange();

        return Listing.builder()
            .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
            .security(security)
            .exchange(exchange)
            .lastRefresh(OffsetDateTime.now())
            .bid(BigDecimal.valueOf(800))
            .ask(BigDecimal.valueOf(1000))
            .contractSize(1)
            .active(true)
            .build();
    }

}
