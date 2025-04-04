package rs.banka4.user_service.exceptions.authenticator;

import org.springframework.http.HttpStatus;
import rs.banka4.rafeisen.common.exceptions.BaseApiException;

public class NotActiveTotpException extends BaseApiException {
    public NotActiveTotpException() {
        super(HttpStatus.BAD_REQUEST, null);
    }
}
