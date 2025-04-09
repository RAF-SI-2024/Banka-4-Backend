package rs.banka4.stock_service.exceptions;

import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import rs.banka4.rafeisen.common.exceptions.BaseApiException;

/**
 * If there is no row in table with stockId, or the ID given is not a Stock
 */
public class StockOwnershipNotFound extends BaseApiException {
    public StockOwnershipNotFound(UUID assetId, UUID userId) {
        super(HttpStatus.NOT_FOUND, Map.of("assetId", assetId, "userId", userId));
    }
}
