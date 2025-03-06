package rs.banka4.user_service.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "DTO for transaction details")
public record CreatePaymentDto(
        @Schema(description = "From account number", example = "102-39443942389")
        String fromAccount,
        @Schema(description = "To account number", example = "102-394438340549")
        String toAccount,
        @Schema(description = "From amount", example = "1.00")
        BigDecimal fromAmount,
        @Schema(description = "Recipient name", example = "Pera Perić")
        String recipient,
        @Schema(description = "Payment code (3-digit, e.g., 2xx)", example = "289")
        String paymentCode,
        @Schema(description = "Reference number", example = "1176926")
        String referenceNumber,
        @Schema(description = "Payment purpose (optional)", example = "za privatni čas")
        String paymentPurpose
) { }
