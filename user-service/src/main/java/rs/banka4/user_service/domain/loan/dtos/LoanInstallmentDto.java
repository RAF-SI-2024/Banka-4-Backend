package rs.banka4.user_service.domain.loan.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import rs.banka4.user_service.domain.loan.db.PaymentStatus;

public record LoanInstallmentDto(
    @Schema(
        description = "Initial amount of the loan",
        example = "100000.00"
    ) BigDecimal installmentAmount,
    @Schema(
        description = "Interest rate",
        example = "4.5"
    ) BigDecimal interestRateAmount,
    @Schema(
        description = "Expected Due Date of the loan installment",
        example = "2031-05-15"
    ) LocalDate expectedDueDate,
    @Schema(
        description = "Actual Due Date of the loan installment",
        example = "2031-05-15"
    ) LocalDate actualDueDate,
    @Schema(
        description = "Payment Status of the loan installment",
        example = "PAID"
    ) PaymentStatus paymentStatus
) {
}
