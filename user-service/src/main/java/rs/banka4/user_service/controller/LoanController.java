package rs.banka4.user_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.banka4.user_service.controller.docs.LoanDocumentation;
import rs.banka4.user_service.domain.loan.dtos.LoanApplicationDto;
import rs.banka4.user_service.service.abstraction.LoanService;

@RestController
@RequestMapping("/loan")
@RequiredArgsConstructor
public class LoanController implements LoanDocumentation {
    LoanService loanService;

    @Override
    @PostMapping("/request")
    public ResponseEntity<Void> createLoanApplication(LoanApplicationDto loanApplicationDto) {
        loanService.createLoanApplication(loanApplicationDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
