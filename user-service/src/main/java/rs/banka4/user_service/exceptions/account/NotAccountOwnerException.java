package rs.banka4.user_service.exceptions.account;

import org.springframework.http.HttpStatus;
import rs.banka4.user_service.exceptions.BaseApiException;

public class NotAccountOwnerException extends BaseApiException {
    public NotAccountOwnerException() {
        super(HttpStatus.FORBIDDEN, null);
    }
}
