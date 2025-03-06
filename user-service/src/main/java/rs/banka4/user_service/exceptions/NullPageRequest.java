package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;


public class NullPageRequest extends BaseApiException{
    public NullPageRequest() {
        super(HttpStatus.BAD_REQUEST, null);
    }
}
