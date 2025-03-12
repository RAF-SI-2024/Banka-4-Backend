package rs.banka4.user_service.domain.loan.db;

import com.fasterxml.jackson.annotation.JsonCreator;
import rs.banka4.user_service.exceptions.account.InvalidCurrency;
import rs.banka4.user_service.exceptions.loan.InvalidLoanStatus;

public enum LoanStatus {
    APPROVED,
    REJECTED,
    PAID_OFF,
    DELAYED,
    PROCESSING;
    @JsonCreator
    public static LoanStatus fromString(String raw) {
        try {
            return LoanStatus.valueOf(raw);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidLoanStatus("Invalid Loan Status: " + raw);
        }
    }
}
