package rs.banka4.user_service.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.domain.loan.dtos.LoanApplicationDto;
import rs.banka4.user_service.domain.loan.dtos.LoanInformationDto;
import rs.banka4.user_service.service.abstraction.LoanService;

public class LoanServiceImpl implements LoanService {

    @Override
    public void createLoanApplication(LoanApplicationDto loanApplicationDto) {

    }

    @Override
    public ResponseEntity<Page<LoanInformationDto>> getAllLoans(PageRequest pageRequest) {
        return null;
    }

    @Override
    public ResponseEntity<Page<LoanInformationDto>> getMyLoans(String token, PageRequest pageRequest) {
        return null;
        // check out /client/search
    }

    @Override
    public void approveLoan(Long loanNumber) {

    }

    @Override
    public void rejectLoan(Long loanNumber) {

    }
}
