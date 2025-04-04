package rs.banka4.user_service.exceptions.transaction;

import java.util.Map;
import org.springframework.http.HttpStatus;
import rs.banka4.rafeisen.common.exceptions.BaseApiException;

public class TransactionNotFound extends BaseApiException {
    public TransactionNotFound(String id) {
        super(HttpStatus.NOT_FOUND, Map.of("id", id));
    }
}
