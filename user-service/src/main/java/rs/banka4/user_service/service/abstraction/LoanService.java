package rs.banka4.user_service.service.abstraction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.domain.loan.dtos.LoanApplicationDto;
import rs.banka4.user_service.domain.loan.dtos.LoanFilterDto;
import rs.banka4.user_service.domain.loan.dtos.LoanInformationDto;

import java.math.BigDecimal;

public interface LoanService {
    void createLoanApplication(LoanApplicationDto loanApplicationDto, String auth);
    ResponseEntity<Page<LoanInformationDto>> getAllLoans(PageRequest pageRequest , LoanFilterDto filterDto);
    ResponseEntity<Page<LoanInformationDto>> getMyLoans(String token, PageRequest pageRequest);
    void approveLoan(Long loanNumber, String auth);
    void rejectLoan(Long loanNumber, String auth);
}
