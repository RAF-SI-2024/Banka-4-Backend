package rs.banka4.user_service.service.abstraction;

import rs.banka4.user_service.domain.loan.db.Loan;
import rs.banka4.user_service.domain.loan.dtos.LoanApplicationDto;

public interface LoanService {
    Loan createLoanApplication(LoanApplicationDto loanApplicationDto);
}
