package rs.banka4.user_service.exceptions.authenticator;

import org.springframework.http.HttpStatus;
import rs.banka4.rafeisen.common.exceptions.BaseApiException;

public class NoTotpException extends BaseApiException {
    public NoTotpException() {
        super(HttpStatus.BAD_REQUEST, null);
    }
}
