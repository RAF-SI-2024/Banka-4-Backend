package rs.banka4.user_service.exceptions.account;

import org.springframework.http.HttpStatus;
import rs.banka4.user_service.exceptions.BaseApiException;

import java.util.Map;

public class InvalidAccountOperationException extends BaseApiException {
    public InvalidAccountOperationException() {
        super(HttpStatus.CONFLICT, null);
    }
}
