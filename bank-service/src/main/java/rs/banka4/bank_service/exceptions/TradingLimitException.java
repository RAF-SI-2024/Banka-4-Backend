package rs.banka4.bank_service.exceptions;

import org.springframework.http.HttpStatus;
import rs.banka4.rafeisen.common.exceptions.BaseApiException;

public class TradingLimitException extends BaseApiException {
    public TradingLimitException() {
        super(HttpStatus.CONFLICT, null);
    }
}
