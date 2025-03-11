package rs.banka4.user_service.domain.loan.dtos;

import rs.banka4.user_service.domain.loan.db.LoanStatus;
import rs.banka4.user_service.domain.loan.db.LoanType;

public record LoanFilterDto(
         LoanType type,
         LoanStatus status,
         String accountNumber
) {
}
