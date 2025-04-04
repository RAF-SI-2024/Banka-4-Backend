package rs.banka4.user_service.exceptions.user;

import org.springframework.http.HttpStatus;
import rs.banka4.rafeisen.common.exceptions.BaseApiException;

public class NotAuthenticated extends BaseApiException {
    public NotAuthenticated() {
        super(HttpStatus.FORBIDDEN, null);
    }
}
