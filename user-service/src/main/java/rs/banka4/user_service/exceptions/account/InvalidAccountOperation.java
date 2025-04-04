package rs.banka4.user_service.exceptions.account;

import org.springframework.http.HttpStatus;
import rs.banka4.rafeisen.common.exceptions.BaseApiException;

public class InvalidAccountOperation extends BaseApiException {
    public InvalidAccountOperation() {
        super(HttpStatus.CONFLICT, null);
    }
}
