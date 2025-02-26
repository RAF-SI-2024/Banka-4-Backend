package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class UserNotFound extends BaseApiException{
    public UserNotFound(String id) {
        super(HttpStatus.NOT_FOUND, Map.of("id", id));
    }
}
