package rs.banka4.user_service.exceptions.card;

import org.springframework.http.HttpStatus;
import rs.banka4.rafeisen.common.exceptions.BaseApiException;

public class DuplicateAuthorizationException extends BaseApiException {
    public DuplicateAuthorizationException() {
        super(HttpStatus.BAD_REQUEST, null);
    }
}
