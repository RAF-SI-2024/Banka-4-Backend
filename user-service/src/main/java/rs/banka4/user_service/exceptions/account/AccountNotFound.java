package rs.banka4.user_service.exceptions.account;

import org.springframework.http.HttpStatus;
import rs.banka4.user_service.exceptions.BaseApiException;

import java.util.Map;

public class AccountNotFound extends BaseApiException {
    public AccountNotFound() {
        super(HttpStatus.NOT_FOUND, Map.of("message", "Account not found"));
    }
}
