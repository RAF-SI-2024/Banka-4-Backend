package rs.banka4.rafeisen.common.exceptions.jwt;

import org.springframework.http.HttpStatus;
import rs.banka4.rafeisen.common.exceptions.BaseApiException;

public class NoJwtProvided extends BaseApiException {

    public NoJwtProvided() {
        super(HttpStatus.FORBIDDEN, null);
    }
}
