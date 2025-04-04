package rs.banka4.user_service.exceptions.user;

import java.util.Map;
import org.springframework.http.HttpStatus;
import rs.banka4.rafeisen.common.exceptions.BaseApiException;

public class UserNotFound extends BaseApiException {
    public UserNotFound(String id) {
        super(HttpStatus.NOT_FOUND, Map.of("id", id));
    }
}
