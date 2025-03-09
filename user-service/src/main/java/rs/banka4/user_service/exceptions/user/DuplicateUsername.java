package rs.banka4.user_service.exceptions.user;

import org.springframework.http.HttpStatus;
import rs.banka4.user_service.exceptions.BaseApiException;

import java.util.Map;

public class DuplicateUsername extends BaseApiException {
    public DuplicateUsername(String username) {
        super(HttpStatus.CONFLICT, Map.of("username", username));
    }
}