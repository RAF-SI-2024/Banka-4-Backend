package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

public class RateLimitExceeded extends BaseApiException {
    public RateLimitExceeded() {
        super(HttpStatus.TOO_MANY_REQUESTS, null);
    }
}
