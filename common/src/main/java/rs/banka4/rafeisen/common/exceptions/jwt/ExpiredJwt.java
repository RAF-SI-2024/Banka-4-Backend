package rs.banka4.rafeisen.common.exceptions.jwt;

import org.springframework.http.HttpStatus;
import rs.banka4.rafeisen.common.exceptions.BaseApiException;

public class ExpiredJwt extends BaseApiException {
    public ExpiredJwt() {
        super(HttpStatus.UNAUTHORIZED, null);
    }
}
