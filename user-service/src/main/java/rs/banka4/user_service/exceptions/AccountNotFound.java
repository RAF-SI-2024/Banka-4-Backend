package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class AccountNotFound extends BaseApiException{

    public AccountNotFound() {
        super(HttpStatus.NOT_FOUND, null);
    }
}
