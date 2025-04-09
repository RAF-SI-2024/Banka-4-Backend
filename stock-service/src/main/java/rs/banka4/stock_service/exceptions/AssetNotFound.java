package rs.banka4.stock_service.exceptions;

import org.springframework.http.HttpStatus;
import rs.banka4.rafeisen.common.exceptions.BaseApiException;

public class AssetNotFound extends BaseApiException {
    public AssetNotFound() {
        super(HttpStatus.NOT_FOUND, null);
    }
}
