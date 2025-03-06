package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

public class InsufficientFunds extends BaseApiException {
    public InsufficientFunds() {
        super(HttpStatus.FORBIDDEN, null);
    }
}
