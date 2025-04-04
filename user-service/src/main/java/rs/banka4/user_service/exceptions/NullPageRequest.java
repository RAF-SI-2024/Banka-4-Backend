package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;
import rs.banka4.rafeisen.common.exceptions.BaseApiException;

public class NullPageRequest extends BaseApiException {
    public NullPageRequest() {
        super(HttpStatus.BAD_REQUEST, null);
    }
}
