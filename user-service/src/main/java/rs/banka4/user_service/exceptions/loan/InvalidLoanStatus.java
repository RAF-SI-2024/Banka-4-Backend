package rs.banka4.user_service.exceptions.loan;

import java.util.Map;
import org.springframework.http.HttpStatus;
import rs.banka4.user_service.exceptions.BaseApiException;

public class InvalidLoanStatus extends BaseApiException {
    public InvalidLoanStatus(String status) {
        super(HttpStatus.NOT_FOUND, Map.of("status", status));
    }
}
