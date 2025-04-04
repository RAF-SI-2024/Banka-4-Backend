package rs.banka4.rafeisen.common.exceptions.jwt;

import org.springframework.http.HttpStatus;
import rs.banka4.rafeisen.common.exceptions.BaseApiException;

public class IllegalArgumentJwt extends BaseApiException {
    public IllegalArgumentJwt() {
        super(HttpStatus.UNAUTHORIZED, null);
    }
}
