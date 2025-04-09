package rs.banka4.stock_service.exceptions;

import java.util.Map;
import org.springframework.http.HttpStatus;
import rs.banka4.rafeisen.common.exceptions.BaseApiException;

public class NegativeLimitException extends BaseApiException {
    public NegativeLimitException(String id) {
        super(HttpStatus.FORBIDDEN, Map.of("id", id));
    }
}
