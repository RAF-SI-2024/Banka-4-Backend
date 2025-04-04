package rs.banka4.rafeisen.common.exceptions.jwt;

import java.util.Map;
import org.springframework.http.HttpStatus;
import rs.banka4.rafeisen.common.exceptions.BaseApiException;

public class Unauthorized extends BaseApiException {
    public Unauthorized(String jwt) {
        super(HttpStatus.UNAUTHORIZED, Map.of("jwt", jwt));
    }
}
