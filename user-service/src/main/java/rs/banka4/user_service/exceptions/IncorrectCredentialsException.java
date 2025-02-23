package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

public class IncorrectCredentialsException extends BaseException {
    public IncorrectCredentialsException() {
        super(ErrorCode.INCORRECT_CREDENTIALS, "Wrong username or password", null, HttpStatus.UNAUTHORIZED);
    }
}
