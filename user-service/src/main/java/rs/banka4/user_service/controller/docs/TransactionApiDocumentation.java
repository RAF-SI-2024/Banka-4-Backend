package rs.banka4.user_service.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import rs.banka4.user_service.domain.transaction.dtos.CreatePaymentDto;
import rs.banka4.user_service.domain.transaction.db.TransactionStatus;
import rs.banka4.user_service.domain.transaction.dtos.TransactionDto;
import rs.banka4.user_service.exceptions.AccountNotFound;
import rs.banka4.user_service.exceptions.ClientNotFound;
import rs.banka4.user_service.exceptions.InsufficientFunds;
import rs.banka4.user_service.exceptions.NotAccountOwner;
import rs.banka4.user_service.exceptions.TransactionNotFound;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Tag(name = "TransactionController", description = "Endpoints for payments")
public interface TransactionApiDocumentation {

    @Operation(
            summary = "Create a new Payment",
            description = "Creates a new payment with the provided details.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Successfully created new payment"),
                    @ApiResponse(responseCode = "400", description = "Bad request - Invalid data or Insufficient funds",
                            content = @Content(schema = @Schema(implementation = InsufficientFunds.class))),
                    @ApiResponse(responseCode = "404", description = "Client not found",
                            content = @Content(schema = @Schema(implementation = ClientNotFound.class))),
                    @ApiResponse(responseCode = "404", description = "Account not found",
                            content = @Content(schema = @Schema(implementation = AccountNotFound.class)))
            }
    )
    ResponseEntity<TransactionDto> createPayment(
            Authentication authentication,
            @Valid CreatePaymentDto createPaymentDto);

    @Operation(
            summary = "Create a new Transfer",
            description = "Creates a new transfer. The client can only transfer using their own account.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Successfully created new payment"),
                    @ApiResponse(responseCode = "400", description = "Bad request - Invalid data or Insufficient funds",
                            content = @Content(schema = @Schema(implementation = InsufficientFunds.class))),
                    @ApiResponse(responseCode = "404", description = "Client not found",
                            content = @Content(schema = @Schema(implementation = ClientNotFound.class))),
                    @ApiResponse(responseCode = "404", description = "Account not found",
                            content = @Content(schema = @Schema(implementation = AccountNotFound.class))),
                    @ApiResponse(responseCode = "403", description = "Not account owner",
                            content = @Content(schema = @Schema(implementation = NotAccountOwner.class)))
            }
    )
    ResponseEntity<TransactionDto> createTransfer(
            Authentication authentication,
            @Valid CreatePaymentDto createPaymentDto);

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
    ResponseEntity<Page<TransactionDto>> getAllTransactionsForClient(
            Authentication auth,
            @Parameter(description = "Payment status") TransactionStatus status,
            @Parameter(description = "Payment amount") BigDecimal amount,
            @Parameter(description = "Payments on date") LocalDate date,
            @Parameter(description = "Account number") String accountNumber,
            @Parameter(description = "Page number") int page,
            @Parameter(description = "Number of employees per page") int size);

    @Operation(
            summary = "Get Transaction by ID",
            description = "Retrieves the transaction with the provided ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved transaction",
                            content = @Content(schema = @Schema(implementation = TransactionDto.class))),
                    @ApiResponse(responseCode = "404", description = "Transaction not found",
                            content = @Content(schema = @Schema(implementation = TransactionNotFound.class)))
            }
    )
    ResponseEntity<TransactionDto> getTransactionById(
            Authentication auth,
            @Parameter(description = "Transaction ID") UUID id);
}
