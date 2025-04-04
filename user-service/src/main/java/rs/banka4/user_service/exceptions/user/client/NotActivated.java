package rs.banka4.user_service.exceptions.user.client;

import org.springframework.http.HttpStatus;
import rs.banka4.rafeisen.common.exceptions.BaseApiException;

public class NotActivated extends BaseApiException {
    public NotActivated() {
        super(HttpStatus.BAD_REQUEST, null);
    }
}
