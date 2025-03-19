package rs.banka4.user_service.exceptions.transaction;

import org.springframework.http.HttpStatus;
import rs.banka4.user_service.exceptions.BaseApiException;

public class ExceededMonthlyLimit extends BaseApiException {
    public ExceededMonthlyLimit() {
        super(HttpStatus.FORBIDDEN, null);
    }
}
