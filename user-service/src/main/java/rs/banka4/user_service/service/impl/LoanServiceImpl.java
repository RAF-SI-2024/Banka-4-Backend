package rs.banka4.user_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.domain.loan.dtos.LoanApplicationDto;
import rs.banka4.user_service.domain.loan.dtos.LoanInformationDto;
import rs.banka4.user_service.repositories.LoanInstallmentRepository;
import rs.banka4.user_service.repositories.LoanRepository;
import rs.banka4.user_service.service.abstraction.LoanService;
import rs.banka4.user_service.utils.loans.LoanRateUtil;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {
    private final LoanRateUtil loanRateUtil;
    private final LoanRepository loanRepository;
    private final LoanInstallmentRepository loanInstallmentRepository;

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
