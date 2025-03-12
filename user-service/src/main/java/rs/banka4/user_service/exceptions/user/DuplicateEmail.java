package rs.banka4.user_service.exceptions.user;

import java.util.Map;
import org.springframework.http.HttpStatus;
import rs.banka4.user_service.exceptions.BaseApiException;

public class DuplicateEmail extends BaseApiException {
    public DuplicateEmail(String email) {
        super(HttpStatus.CONFLICT, Map.of("email", email));
    }
}
