package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

public class NotValidTotpException extends BaseApiException {
    public NotValidTotpException() {
        super(HttpStatus.BAD_REQUEST, null);
    }}
