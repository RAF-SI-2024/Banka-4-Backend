package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

public class AuthorizationException extends BaseApiException {
    public AuthorizationException() {
        super(HttpStatus.UNAUTHORIZED, null);
    }
}