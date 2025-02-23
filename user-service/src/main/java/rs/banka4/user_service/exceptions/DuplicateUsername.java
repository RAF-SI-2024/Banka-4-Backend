package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class DuplicateUsername extends BaseApiException {
    public DuplicateUsername(String username) {
        super(HttpStatus.CONFLICT, Map.of("username", username));
    }
}