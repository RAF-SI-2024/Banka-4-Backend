package rs.banka4.user_service.exceptions.loan;

import java.util.Map;
import org.springframework.http.HttpStatus;
import rs.banka4.user_service.exceptions.BaseApiException;

public class InvalidLoanType extends BaseApiException {
    public InvalidLoanType(String type) {
        super(HttpStatus.NOT_FOUND, Map.of("type", type));
    }
}
