package rs.banka4.user_service.exceptions.loan;

import org.springframework.http.HttpStatus;
import rs.banka4.user_service.exceptions.BaseApiException;

import java.util.Map;

public class NoLoansOnAccount extends BaseApiException {
    public NoLoansOnAccount(String email) {
       super(HttpStatus.NOT_FOUND, Map.of("email",email));
    }
}
