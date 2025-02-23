package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class DuplicateUsernameException extends BaseException {
    public DuplicateUsernameException(String username) {
        super(ErrorCode.DUPLICATE_USERNAME, "Username already taken", Map.of("username", username),
                HttpStatus.CONFLICT
        );
    }
}