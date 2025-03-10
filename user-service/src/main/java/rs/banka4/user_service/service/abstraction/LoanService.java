package rs.banka4.user_service.service.abstraction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.domain.loan.dtos.LoanApplicationDto;
import rs.banka4.user_service.domain.loan.dtos.LoanInformationDto;

public interface LoanService {
    void createLoanApplication(LoanApplicationDto loanApplicationDto);
    ResponseEntity<Page<LoanInformationDto>> getAllLoans(PageRequest pageRequest);
    ResponseEntity<Page<LoanInformationDto>> getMyLoans(String token, PageRequest pageRequest);
    void approveLoan(Long loanNumber);
    void rejectLoan(Long loanNumber);
}
