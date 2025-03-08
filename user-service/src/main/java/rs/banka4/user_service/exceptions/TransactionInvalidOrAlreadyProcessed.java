package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

public class TransactionInvalidOrAlreadyProcessed extends BaseApiException {
    public TransactionInvalidOrAlreadyProcessed() {
        super(HttpStatus.BAD_REQUEST, null);
    }
}
