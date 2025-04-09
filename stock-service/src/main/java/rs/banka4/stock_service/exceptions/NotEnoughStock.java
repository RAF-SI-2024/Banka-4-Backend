package rs.banka4.stock_service.exceptions;

import org.springframework.http.HttpStatus;
import rs.banka4.rafeisen.common.exceptions.BaseApiException;

/**
 * There is not enough stocks on your private/public account to do the transfer
 */
public class NotEnoughStock extends BaseApiException {
    public NotEnoughStock() {
        super(HttpStatus.BAD_REQUEST, null);
    }
}
