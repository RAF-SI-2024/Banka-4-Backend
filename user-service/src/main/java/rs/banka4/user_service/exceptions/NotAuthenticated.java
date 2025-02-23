package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

public class NotAuthenticated extends BaseApiException {
    public NotAuthenticated() {
        super(HttpStatus.FORBIDDEN, null);
    }
}