package rs.banka4.stock_service.domain.listing.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public record ListingInfoDto(
    UUID securityId,
    String name,
    String ticker,
    Integer volume,
    BigDecimal change,
    BigDecimal price
) {
}
