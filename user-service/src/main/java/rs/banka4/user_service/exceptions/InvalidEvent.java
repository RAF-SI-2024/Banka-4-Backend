package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidEvent extends BaseApiException {
    public InvalidEvent() { super(HttpStatus.BAD_REQUEST, null); }
}