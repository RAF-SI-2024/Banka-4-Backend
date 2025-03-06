package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

public class NotAccountOwner extends BaseApiException {
    public NotAccountOwner() {
        super(HttpStatus.FORBIDDEN, null);
    }
}
