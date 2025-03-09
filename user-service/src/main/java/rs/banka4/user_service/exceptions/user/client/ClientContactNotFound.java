package rs.banka4.user_service.exceptions.user.client;

import org.springframework.http.HttpStatus;
import rs.banka4.user_service.exceptions.BaseApiException;

public class ClientContactNotFound  extends BaseApiException {
    public ClientContactNotFound() {
        super(HttpStatus.NOT_FOUND, null);
    }
}
