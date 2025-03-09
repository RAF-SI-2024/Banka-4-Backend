package rs.banka4.user_service.exceptions.user.client;

import org.springframework.http.HttpStatus;
import rs.banka4.user_service.exceptions.BaseApiException;

public class ContactNotFound extends BaseApiException {
    public ContactNotFound() {
        super(HttpStatus.NOT_FOUND, null);
    }
}
