package rs.banka4.user_service.domain.loan.db;

import com.fasterxml.jackson.annotation.JsonCreator;
import rs.banka4.user_service.domain.currency.db.Currency;
import rs.banka4.user_service.exceptions.account.InvalidCurrency;
import rs.banka4.user_service.exceptions.loan.InvalidLoanType;

public enum LoanType {
    CASH,
    MORTGAGE,
    AUTO_LOAN,
    REFINANCING,
    STUDENT_LOAN;
    @JsonCreator
    public static LoanType fromString(String raw) {
        try {
            return LoanType.valueOf(raw);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidLoanType("Invalid Loan Type: " + raw);
        }
    }
}
