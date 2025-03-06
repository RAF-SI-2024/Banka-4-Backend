package rs.banka4.user_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.banka4.user_service.dto.*;
import rs.banka4.user_service.dto.requests.CreatePaymentDto;
import rs.banka4.user_service.service.abstraction.PaymentService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
@Tag(name = "PaymentController", description = "Endpoints for payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(
            summary = "Create a new Payment",
            description = "Creates a new payment with the provided details.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Successfully created new payment"),
                    @ApiResponse(responseCode = "400", description = "Bad request - Invalid data")
            }
    )
    @PostMapping("/payment")
    public ResponseEntity<TransactionDto> createPayment(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Details of the new client to create", required = true)
            Authentication authentication,
            @RequestBody @Valid CreatePaymentDto createPaymentDto) {
        return paymentService.createPayment(authentication, createPaymentDto);
    }

    @Operation(
            summary = "Create a new Transfer",
            description = "Creates a new transfer. The client can only transfer using their own account.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Successfully created new payment"),
                    @ApiResponse(responseCode = "400", description = "Bad request - Invalid data")
            }
    )
    @PostMapping("/transfer")
    public ResponseEntity<TransactionDto> createTransfer(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Details of the new client to create", required = true)
            Authentication authentication,
            @RequestBody @Valid CreatePaymentDto createPaymentDto) {
        return paymentService.createTransfer(authentication, createPaymentDto);
    }

    @Operation(
            summary = "Get Client Payments",
            description = "Retrieves the list of payments for the authenticated client. Requires authentication.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved payments",
                            content = @Content(schema = @Schema(implementation = TransactionDto.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Token errors"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Access denied")
            }
    )
    @GetMapping("/search")
    public ResponseEntity<Page<TransactionDto>> getPaymentsForClient(
            Authentication auth,
            @RequestParam(required = false) @Parameter(description = "Payment status") PaymentStatus status,
            @RequestParam(required = false) @Parameter(description = "Payment amount") BigDecimal amount,
            @RequestParam(required = false) @Parameter(description = "Payments on date") LocalDate date,
            @RequestParam(required = false) @Parameter(description = "Account number") String accountNumber,
            @RequestParam(defaultValue = "0") @Parameter(description = "Page number") int page,
            @RequestParam(defaultValue = "10") @Parameter(description = "Number of employees per page") int size
    ){
        return this.paymentService.getAllPaymentsForClient(auth.getCredentials().toString(), status, amount, date, accountNumber, PageRequest.of(page, size));
    }

    @Operation(
            summary = "Get Transaction by ID",
            description = "Retrieves the transaction with the provided ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved transaction",
                            content = @Content(schema = @Schema(implementation = TransactionDto.class))),
                    @ApiResponse(responseCode = "404", description = "Not found - Transaction with the provided ID does not exist")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> getTransactionById(Authentication auth, @PathVariable UUID id){
        return this.paymentService.getTransactionById(auth.getCredentials().toString(), id);
    }

}