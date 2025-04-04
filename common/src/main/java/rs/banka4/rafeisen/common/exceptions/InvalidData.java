package rs.banka4.rafeisen.common.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidData extends BaseApiException {
    public InvalidData() {
        super(HttpStatus.BAD_REQUEST, null);
    }
}
