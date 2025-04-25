package rs.banka4.bank_service.exceptions;

import org.springframework.http.HttpStatus;
import rs.banka4.rafeisen.common.exceptions.BaseApiException;

public class InterbankError extends BaseApiException {
    public InterbankError() {
        super(HttpStatus.INTERNAL_SERVER_ERROR, null);
    }
}
