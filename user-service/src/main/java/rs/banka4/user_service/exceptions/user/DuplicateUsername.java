package rs.banka4.user_service.exceptions.user;

import java.util.Map;
import org.springframework.http.HttpStatus;
import rs.banka4.rafeisen.common.exceptions.BaseApiException;

public class DuplicateUsername extends BaseApiException {
    public DuplicateUsername(String username) {
        super(HttpStatus.CONFLICT, Map.of("username", username));
    }
}
