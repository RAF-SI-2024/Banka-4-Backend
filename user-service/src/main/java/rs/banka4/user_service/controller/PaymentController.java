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
import java.util.List;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Tag(name = "PaymentController", description = "Endpoints for payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(
            summary = "Create a new Payment",
            description = "Creates a new client with the provided details and a list of account details.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Successfully created new payment"),
                    @ApiResponse(responseCode = "400", description = "Bad request - Invalid data")
            }
    )
    @PostMapping
    public ResponseEntity<PaymentDto> createPayment(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Details of the new client to create", required = true)
            @RequestBody @Valid CreatePaymentDto createPaymentDto) {
        return paymentService.createPayment(createPaymentDto);
    }

    @Operation(
            summary = "Get Client Payments",
            description = "Retrieves the list of payments for the authenticated client. Requires authentication.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved payments",
                            content = @Content(schema = @Schema(implementation = PaymentDto.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Token errors"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Access denied")
            }
    )
    @GetMapping("/search")
    public ResponseEntity<Page<PaymentDto>> getPaymentsForClient(
            Authentication auth,
            @RequestParam(required = false) @Parameter(description = "Payment status") PaymentStatus status,
            @RequestParam(required = false) @Parameter(description = "Payment amount") BigDecimal amount,
            @RequestParam(required = false) @Parameter(description = "Payments on date") LocalDate date,
            @RequestParam(defaultValue = "0") @Parameter(description = "Page number") int page,
            @RequestParam(defaultValue = "10") @Parameter(description = "Number of employees per page") int size
            ){
        return this.paymentService.getPaymentsForClient(auth.getCredentials().toString(), status, amount, date, PageRequest.of(page, size));
    }

}
