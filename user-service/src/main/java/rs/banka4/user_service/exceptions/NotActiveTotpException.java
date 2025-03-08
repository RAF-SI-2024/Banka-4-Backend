package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

public class NotActiveTotpException extends BaseApiException {
    public NotActiveTotpException() {
        super(HttpStatus.BAD_REQUEST, null);
    }
}
