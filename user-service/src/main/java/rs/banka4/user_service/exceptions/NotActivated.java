package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

public class NotActivated extends BaseApiException  {
    public NotActivated() {
        super(HttpStatus.BAD_REQUEST, null);
    }
}
