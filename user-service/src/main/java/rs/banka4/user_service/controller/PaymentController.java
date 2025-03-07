package rs.banka4.user_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.banka4.user_service.controller.docs.PaymentApiDocumentation;
import rs.banka4.user_service.dto.requests.CreatePaymentDto;
import rs.banka4.user_service.dto.PaymentStatus;
import rs.banka4.user_service.dto.TransactionDto;
import rs.banka4.user_service.service.abstraction.PaymentService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
public class PaymentController implements PaymentApiDocumentation {

    private final PaymentService paymentService;

    @Override
    @PostMapping("/payment")
    public ResponseEntity<TransactionDto> createPayment(Authentication authentication,
                                                        @RequestBody @Valid CreatePaymentDto createPaymentDto) {
        return paymentService.createPayment(authentication, createPaymentDto);
    }

    @Override
    @PostMapping("/transfer")
    public ResponseEntity<TransactionDto> createTransfer(Authentication authentication,
                                                         @RequestBody @Valid CreatePaymentDto createPaymentDto) {
        return paymentService.createTransfer(authentication, createPaymentDto);
    }

    @Override
    @GetMapping("/search")
    public ResponseEntity<Page<TransactionDto>> getPaymentsForClient(
            Authentication auth,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) BigDecimal amount,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) String accountNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return paymentService.getAllPaymentsForClient(auth.getCredentials().toString(),
                status, amount, date, accountNumber, PageRequest.of(page, size));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> getTransactionById(Authentication auth, @PathVariable UUID id) {
        return paymentService.getTransactionById(auth.getCredentials().toString(), id);
    }
}
