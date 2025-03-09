package rs.banka4.user_service.exceptions.transaction;

import org.springframework.http.HttpStatus;
import rs.banka4.user_service.exceptions.BaseApiException;

import java.util.Map;

public class TransactionNotFound extends BaseApiException {
    public TransactionNotFound(String id) {
        super(HttpStatus.NOT_FOUND, Map.of("id", id));
    }
} 