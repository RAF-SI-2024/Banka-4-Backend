package rs.banka4.user_service.exceptions.card;

import org.springframework.http.HttpStatus;
import rs.banka4.user_service.exceptions.BaseApiException;

public class NotValidCardStatus extends BaseApiException {
    public NotValidCardStatus() {
        super(HttpStatus.BAD_REQUEST, null);
    }
}
