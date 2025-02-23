package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

public class IncorrectCredentials extends BaseApiException {
    public IncorrectCredentials() {
        super(HttpStatus.UNAUTHORIZED, null);
    }
}
