package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

public class NotAuthenticatedException extends BaseException {
    public NotAuthenticatedException() {
        super(ErrorCode.NOT_AUTHENTICATED, "User is not authenticated", null, HttpStatus.FORBIDDEN);
    }
}