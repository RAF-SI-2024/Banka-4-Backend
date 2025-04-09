package rs.banka4.stock_service.exceptions;

import org.springframework.http.HttpStatus;
import rs.banka4.rafeisen.common.exceptions.BaseApiException;

public class ExchangeNotFound extends BaseApiException {
    public ExchangeNotFound() {
        super(HttpStatus.NOT_FOUND, null);
    }
}
