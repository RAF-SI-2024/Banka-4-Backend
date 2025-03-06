package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class TransactionNotFound extends BaseApiException{
    public TransactionNotFound(String id) {
        super(HttpStatus.NOT_FOUND, Map.of("id", id));
    }
} 