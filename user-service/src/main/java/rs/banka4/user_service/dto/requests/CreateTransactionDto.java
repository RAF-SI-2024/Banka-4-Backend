package rs.banka4.user_service.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "DTO for creating payment order")
public record CreateTransactionDto(
        @NotBlank(message = "From account number is required and cannot be blank.")
        @Schema(description = "From account number", example = "102-39443942389")
        String fromAccount,

        @NotBlank(message = "To account number is required and cannot be blank.")
        @Schema(description = "To account number", example = "102-394438340549")
        String toAccount,

        @NotNull(message = "From amount is required and cannot be null.")
        @Schema(description = "From amount", example = "1.00")
        BigDecimal fromAmount,

        @NotBlank(message = "From currency is required and cannot be blank.")
        @Schema(description = "From currency", example = "EUR")
        String fromCurrency,

        @NotBlank(message = "Recipient name is required and cannot be blank.")
        @Schema(description = "Recipient name", example = "Pera Perić")
        String recipient,

        @NotBlank(message = "Payment code is required and must be a 3-digit code (e.g., 2xx).")
        @Schema(description = "Payment code (3-digit, e.g., 2xx)", example = "289")
        String paymentCode,

        @NotBlank(message = "Reference number is required and cannot be blank.")
        @Schema(description = "Reference number", example = "1176926")
        String referenceNumber,

        @NotBlank(message = "Payment purpose is required and cannot be blank.")
        @Schema(description = "Payment purpose (optional)", example = "za privatni čas")
        String paymentPurpose
) { }
