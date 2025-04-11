package rs.banka4.stock_service.domain.security.responses;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "The different types of securities available.")
public enum SecurityTypeDto {
    STOCK("Stock"),
    FUTURE("Future"),
    FOREX("Forex"),;

    SecurityTypeDto(String displayName) {
    }
}
