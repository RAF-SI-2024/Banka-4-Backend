package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class RateLimitExceeded extends BaseApiException {
    public RateLimitExceeded() {
        super(HttpStatus.TOO_MANY_REQUESTS, Map.of("message", "Too many requests"));
    }
}
