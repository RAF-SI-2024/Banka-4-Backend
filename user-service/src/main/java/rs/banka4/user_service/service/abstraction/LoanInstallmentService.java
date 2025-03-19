package rs.banka4.user_service.service.abstraction;

import org.springframework.data.domain.Page;
import rs.banka4.user_service.domain.loan.dtos.LoanInstallmentDto;

public interface LoanInstallmentService {
    Page<LoanInstallmentDto> getInstallmentsForLoan(
        Long loanNumber,
        int page,
        int size,
        String auth
    );
}
