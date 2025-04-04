package rs.banka4.rafeisen.common.exceptions.jwt;

import org.springframework.http.HttpStatus;
import rs.banka4.rafeisen.common.exceptions.BaseApiException;

public class RefreshTokenRevoked extends BaseApiException {
    public RefreshTokenRevoked() {
        super(HttpStatus.CONFLICT, null);
    }
}
