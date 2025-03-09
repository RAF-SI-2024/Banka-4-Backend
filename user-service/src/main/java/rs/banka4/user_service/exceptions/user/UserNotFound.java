package rs.banka4.user_service.exceptions.user;

import org.springframework.http.HttpStatus;
import rs.banka4.user_service.exceptions.BaseApiException;

import java.util.Map;

public class UserNotFound extends BaseApiException {
    public UserNotFound(String id) {
        super(HttpStatus.NOT_FOUND, Map.of("id", id));
    }
}
