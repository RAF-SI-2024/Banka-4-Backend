package rs.banka4.user_service.exceptions.card;

import org.springframework.http.HttpStatus;
import rs.banka4.user_service.exceptions.BaseApiException;

public class AuthorizedUserNotAllowed extends BaseApiException {
    public AuthorizedUserNotAllowed() {
        super(HttpStatus.BAD_REQUEST, null);
    }
}
