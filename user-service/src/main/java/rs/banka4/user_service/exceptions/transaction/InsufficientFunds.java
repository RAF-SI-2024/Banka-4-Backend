package rs.banka4.user_service.exceptions.transaction;

import org.springframework.http.HttpStatus;
import rs.banka4.user_service.exceptions.BaseApiException;

public class InsufficientFunds extends BaseApiException {
    public InsufficientFunds() {
        super(HttpStatus.FORBIDDEN, null);
    }
}
