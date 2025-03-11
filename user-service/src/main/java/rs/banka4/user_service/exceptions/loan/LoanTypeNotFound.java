package rs.banka4.user_service.exceptions.loan;

import org.springframework.http.HttpStatus;
import rs.banka4.user_service.exceptions.BaseApiException;

public class LoanTypeNotFound extends BaseApiException {

    public LoanTypeNotFound() {
        super(HttpStatus.NOT_FOUND, null);
    }
}
