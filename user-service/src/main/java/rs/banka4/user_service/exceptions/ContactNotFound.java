package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

public class ContactNotFound extends BaseApiException {
    public ContactNotFound() {
        super(HttpStatus.NOT_FOUND, null);
    }
}
