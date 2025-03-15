package rs.banka4.user_service.exceptions.loan;

import org.springframework.http.HttpStatus;
import rs.banka4.user_service.exceptions.BaseApiException;

import java.util.Map;

public class InterestRateAmountNotSupported extends BaseApiException {
    public InterestRateAmountNotSupported() {
        super(HttpStatus.CONFLICT, null);
    }
}
