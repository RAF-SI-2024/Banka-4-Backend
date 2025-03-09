package rs.banka4.user_service.service.mock;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.domain.loan.db.Loan;
import rs.banka4.user_service.domain.loan.dtos.LoanApplicationDto;
import rs.banka4.user_service.service.abstraction.LoanService;

@Service
@Primary
public class LoanServiceMock implements LoanService {
    @Override
    public Loan createLoanApplication(LoanApplicationDto loanApplicationDto) {
        return null;
    }
}
