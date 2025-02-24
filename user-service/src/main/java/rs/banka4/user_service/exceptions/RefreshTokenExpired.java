package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class RefreshTokenExpired extends BaseApiException {
    public RefreshTokenExpired() {
        super(HttpStatus.CONFLICT, null);
    }
}
