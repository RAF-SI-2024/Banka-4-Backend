package rs.banka4.user_service.exceptions.loan;

import java.util.Map;
import org.springframework.http.HttpStatus;
import rs.banka4.rafeisen.common.exceptions.BaseApiException;
import rs.banka4.user_service.domain.loan.db.Loan;

/**
 * Thrown when attempting to approve or deny a loan that has already been approved or denied.
 */
public class LoanAlreadyJudged extends BaseApiException {
    public LoanAlreadyJudged(Loan loan) {
        super(HttpStatus.BAD_REQUEST, Map.of("id", loan.getId()));
    }
}
