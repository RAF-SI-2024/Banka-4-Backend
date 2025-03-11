package rs.banka4.user_service.exceptions.jwt;

import org.springframework.http.HttpStatus;
import rs.banka4.user_service.exceptions.BaseApiException;

import java.util.Map;

public class Unauthorized extends BaseApiException {
    public Unauthorized(String jwt) {
        super(HttpStatus.UNAUTHORIZED, Map.of("jwt", jwt));
    }
}
