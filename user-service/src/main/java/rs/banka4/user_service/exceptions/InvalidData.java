package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidData extends BaseApiException {
    public InvalidData() {
        super(HttpStatus.BAD_REQUEST, null);
    }
}
