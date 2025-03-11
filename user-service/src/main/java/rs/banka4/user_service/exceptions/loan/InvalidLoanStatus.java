package rs.banka4.user_service.exceptions.loan;

import org.springframework.http.HttpStatus;
import rs.banka4.user_service.exceptions.BaseApiException;

import java.util.Map;

public class InvalidLoanStatus extends BaseApiException {
    public InvalidLoanStatus(String status) {
        super(HttpStatus.NOT_FOUND, Map.of("status",status));
    }


}
