package rs.banka4.user_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
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

    private final TransactionService transactionService;

    @Override
    @PostMapping("/payment")
    public ResponseEntity<TransactionDto> createTransaction(Authentication authentication, @RequestBody @Valid CreatePaymentDto createPaymentDto) {
       TransactionDto transactionDto = transactionService.createTransaction(authentication, createPaymentDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionDto);
    }

    @Override
    @PostMapping("/transfer")
    public ResponseEntity<TransactionDto> createTransfer(Authentication authentication, @RequestBody @Valid CreatePaymentDto createPaymentDto) {
        TransactionDto transactionDto = transactionService.createTransfer(authentication, createPaymentDto);
        return ResponseEntity.ok(transactionDto);
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
        Page<TransactionDto> transactions = transactionService.getAllTransactionsForClient(auth.getCredentials().toString(),
                status, amount, date, accountNumber, PageRequest.of(page, size));
        return ResponseEntity.ok(transactions);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> getTransactionById(Authentication auth, @PathVariable UUID id) {
        TransactionDto transactionDto = transactionService.getTransactionById(auth.getCredentials().toString(), id);
        return ResponseEntity.ok(transactionDto);
    }
}
