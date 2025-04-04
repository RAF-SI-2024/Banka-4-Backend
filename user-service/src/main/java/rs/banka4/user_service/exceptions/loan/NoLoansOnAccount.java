package rs.banka4.user_service.exceptions.loan;

import java.util.Map;
import org.springframework.http.HttpStatus;
import rs.banka4.rafeisen.common.exceptions.BaseApiException;

public class NoLoansOnAccount extends BaseApiException {
    public NoLoansOnAccount(String email) {
        super(HttpStatus.NOT_FOUND, Map.of("email", email));
    }
}
