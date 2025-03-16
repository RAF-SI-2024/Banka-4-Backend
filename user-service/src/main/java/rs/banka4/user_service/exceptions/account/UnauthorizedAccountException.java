package rs.banka4.user_service.exceptions.account;

import org.springframework.http.HttpStatus;
import rs.banka4.user_service.exceptions.BaseApiException;

import java.util.Map;

public class UnauthorizedAccountException extends BaseApiException {
    public UnauthorizedAccountException() {
        super(HttpStatus.FORBIDDEN, Map.of("message", "You don't own this account"));
    }
}
