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
import rs.banka4.user_service.domain.loan.db.LoanStatus;
import rs.banka4.user_service.domain.loan.db.LoanType;
import rs.banka4.user_service.domain.loan.dtos.LoanApplicationDto;
import rs.banka4.user_service.domain.loan.dtos.LoanFilterDto;
import rs.banka4.user_service.domain.loan.dtos.LoanInformationDto;
import rs.banka4.user_service.service.abstraction.LoanService;

@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
public class LoanController implements LoanDocumentation {
    private final LoanService loanService;

    @Override
    @PostMapping
    public ResponseEntity<Void> createLoanApplication(@RequestBody @Valid LoanApplicationDto loanApplicationDto,
                                                      Authentication auth) {

        loanService.createLoanApplication(loanApplicationDto, (String) auth.getCredentials());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @GetMapping("/search")
    public ResponseEntity<Page<LoanInformationDto>> getAllLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String loanType,
            @RequestParam(required = false) String loanStatus,
            @RequestParam(required = false) String accountNumber) {
        LoanFilterDto filter = new LoanFilterDto(
                (loanType != null && !loanType.isEmpty()) ? LoanType.fromString(loanType) : null,
                (loanStatus != null && !loanStatus.isEmpty()) ? LoanStatus.fromString(loanStatus) : null,
                accountNumber
        );


        return loanService.getAllLoans(PageRequest.of(page, size), filter);
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<Page<LoanInformationDto>> me(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return loanService.getMyLoans(auth.getCredentials().toString(), PageRequest.of(page, size));
    }

    @Override
    @PutMapping("/approve/{loanNumber}")
    public ResponseEntity<Void> approveLoan(@PathVariable Long loanNumber, Authentication auth) {
        loanService.approveLoan(loanNumber, (String) auth.getCredentials());
        return ResponseEntity.ok().build();
    }

    @Override
    @PutMapping("/reject/{loanNumber}")
    public ResponseEntity<Void> rejectLoan(@PathVariable Long loanNumber, Authentication auth) {
        loanService.rejectLoan(loanNumber, (String) auth.getCredentials());
        return ResponseEntity.ok().build();
    }
}
