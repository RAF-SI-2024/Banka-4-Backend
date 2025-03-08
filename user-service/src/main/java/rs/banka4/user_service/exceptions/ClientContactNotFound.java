package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

public class ClientContactNotFound  extends BaseApiException{
    public ClientContactNotFound() {
        super(HttpStatus.NOT_FOUND, null);
    }
}
