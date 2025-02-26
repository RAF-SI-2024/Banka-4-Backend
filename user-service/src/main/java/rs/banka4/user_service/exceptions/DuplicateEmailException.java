package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class DuplicateEmailException extends BaseApiException{
    public DuplicateEmailException(String email) {
        super(HttpStatus.CONFLICT, Map.of("email",email));
    }
}
