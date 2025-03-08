package rs.banka4.user_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.banka4.user_service.controller.docs.TransactionApiDocumentation;
import rs.banka4.user_service.domain.transaction.dtos.CreatePaymentDto;
import rs.banka4.user_service.domain.transaction.db.TransactionStatus;
import rs.banka4.user_service.domain.transaction.dtos.TransactionDto;
import rs.banka4.user_service.service.abstraction.TransactionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
public class TransactionController implements TransactionApiDocumentation {

    private final TransactionService paymentService;

    @Override
    @PostMapping("/payment")
    public ResponseEntity<TransactionDto> createPayment(Authentication authentication, @RequestBody @Valid CreatePaymentDto createPaymentDto) {
        return paymentService.createPayment(authentication, createPaymentDto);
    }

    @Override
    @PostMapping("/transfer")
    public ResponseEntity<TransactionDto> createTransfer(Authentication authentication, @RequestBody @Valid CreatePaymentDto createPaymentDto) {
        return paymentService.createTransfer(authentication, createPaymentDto);
    }

    @Override
    @GetMapping("/search")
    public ResponseEntity<Page<TransactionDto>> getAllTransactionsForClient(
            Authentication auth,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) BigDecimal amount,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) String accountNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return paymentService.getAllTransactionsForClient(auth.getCredentials().toString(),
                status, amount, date, accountNumber, PageRequest.of(page, size));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> getTransactionById(Authentication auth, @PathVariable UUID id) {
        return paymentService.getTransactionById(auth.getCredentials().toString(), id);
    }
}
