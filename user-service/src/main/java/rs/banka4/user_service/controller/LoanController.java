package rs.banka4.user_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.banka4.user_service.controller.docs.LoanDocumentation;
import rs.banka4.user_service.domain.loan.dtos.LoanApplicationDto;
import rs.banka4.user_service.domain.loan.dtos.LoanInformationDto;
import rs.banka4.user_service.service.abstraction.LoanService;

@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
public class LoanController implements LoanDocumentation {
    private final LoanService loanService;

    @Override
    @PostMapping
    public ResponseEntity<Void> createLoanApplication(@RequestBody @Valid LoanApplicationDto loanApplicationDto) {
        loanService.createLoanApplication(loanApplicationDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @GetMapping("/search")
    public ResponseEntity<Page<LoanInformationDto>> getAllLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        return loanService.getAllLoans(PageRequest.of(page, size));
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<Page<LoanInformationDto>> me(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        return loanService.getMyLoans(auth.getCredentials().toString(), PageRequest.of(page, size));
    }

    @Override
    @PutMapping("/approve/{loanNumber}")
    public ResponseEntity<Void> approveLoan(@PathVariable Long loanNumber) {
        loanService.approveLoan(loanNumber);
        return ResponseEntity.ok().build();
    }

    @Override
    @PutMapping("/reject/{loanNumber}")
    public ResponseEntity<Void> rejectLoan(@PathVariable Long loanNumber) {
        loanService.rejectLoan(loanNumber);
        return ResponseEntity.ok().build();
    }
}
