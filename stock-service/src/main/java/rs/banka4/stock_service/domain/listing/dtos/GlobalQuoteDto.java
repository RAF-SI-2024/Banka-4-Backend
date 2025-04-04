package rs.banka4.stock_service.domain.listing.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GlobalQuoteDto(
    @JsonProperty("05. price") String priceStr,

    @JsonProperty("06. volume") String volumeStr
) {
}
