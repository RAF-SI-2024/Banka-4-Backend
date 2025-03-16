package rs.banka4.user_service.exceptions.account;

import org.springframework.http.HttpStatus;
import rs.banka4.user_service.exceptions.BaseApiException;

import java.util.Map;

public class InvalidAccountOperationException extends BaseApiException {
    public InvalidAccountOperationException(String reason) {
        super(HttpStatus.CONFLICT, Map.of(
                "message", "Cannot modify account limits",
                "reason", reason
        ));
    }
}
