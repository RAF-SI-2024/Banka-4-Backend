package rs.banka4.user_service.exceptions.card;

import org.springframework.http.HttpStatus;
import rs.banka4.rafeisen.common.exceptions.BaseApiException;

public class CardLimitExceededException extends BaseApiException {
    public CardLimitExceededException() {
        super(HttpStatus.FORBIDDEN, null);
    }
}
