package rs.banka4.stock_service.exceptions;

import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import rs.banka4.rafeisen.common.exceptions.BaseApiException;

public class ListingNotFoundException extends BaseApiException {
    public ListingNotFoundException(UUID id) {
        super(HttpStatus.NOT_FOUND, Map.of("id", id));
    }
}
