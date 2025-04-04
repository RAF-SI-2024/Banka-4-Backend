package rs.banka4.user_service.exceptions.loan;

import org.springframework.http.HttpStatus;
import rs.banka4.rafeisen.common.exceptions.BaseApiException;

public class InterestRateAmountNotSupported extends BaseApiException {
    public InterestRateAmountNotSupported() {
        super(HttpStatus.CONFLICT, null);
    }
}
