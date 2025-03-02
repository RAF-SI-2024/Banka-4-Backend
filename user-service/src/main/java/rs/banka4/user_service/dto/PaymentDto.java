package rs.banka4.user_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "DTO for payment order details")
public record PaymentDto(
    @Schema(description = "Payment Order ID", example = "e2a1f6f3-9f74-4b8a-bc9a-2f3a5c6d7e8f")
    String id,
    @Schema(description = "Payment order number", example = "1265463698391")
    String orderNumber,
    @Schema(description = "From account number", example = "102-39443942389")
    String fromAccount,
    @Schema(description = "To account number", example = "102-394438340549")
    String toAccount,
    @Schema(description = "Payment amount", example = "1.00 EUR (fromAccount currency is used)")
    BigDecimal amount,
    @Schema(description = "Recipient name", example = "Pera Perić")
    String recipient,
    @Schema(description = "Payment code (3-digit, e.g., 2xx)", example = "289")
    String paymentCode,
    @Schema(description = "Reference number", example = "1176926")
    String referenceNumber,
    @Schema(description = "Payment purpose (optional)", example = "za privatni čas")
    String paymentPurpose,
    @Schema(description = "Payment date and time", example = "2023-05-01T12:30:00")
    LocalDateTime paymentDateTime,
    @Schema(description = "Payment status", example = "REALIZED")
    PaymentStatus status
) { }
