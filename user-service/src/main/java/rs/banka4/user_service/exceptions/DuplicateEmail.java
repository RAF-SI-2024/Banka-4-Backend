package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class DuplicateEmail extends BaseApiException {
    public DuplicateEmail(String email) {
        super(HttpStatus.CONFLICT, Map.of("email", email));
    }
}
