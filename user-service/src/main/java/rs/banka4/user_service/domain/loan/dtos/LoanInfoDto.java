package rs.banka4.user_service.domain.loan.dtos;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import rs.banka4.user_service.domain.currency.dtos.CurrencyDto;
import rs.banka4.user_service.domain.loan.db.Loan;
import rs.banka4.user_service.domain.loan.db.LoanStatus;
import rs.banka4.user_service.domain.loan.db.LoanType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Getter
@Setter
public class LoanInfoDto {

    @Schema(description = "Unique identifier for the loan", example = "1234567")
    private Long loanNumber;

    @Schema(description = "Type of loan", example = "CASH")
    private LoanType type;

    @Schema(description = "Total amount of the loan", example = "100000.00")
    private BigDecimal amount;

    @Schema(description = "Number of months/installments for repayment", example = "120")
    private Integer repaymentPeriod;

    @Schema(description = "Interest rate at the time of credit creation", example = "4.5")
    private BigDecimal baseInterestRate;

    @Schema(description = "Current effective interest rate", example = "5.0")
    private BigDecimal effectiveInterestRate;

    @Schema(description = "Date the loan agreement was signed", example = "2021-05-15")
    private LocalDate agreementDate;

    @Schema(description = "Date by which the loan should be fully paid off", example = "2031-05-15")
    private LocalDate dueDate;

    @Schema(description = "Amount of the next installment to be paid", example = "950.00")
    private BigDecimal nextInstallmentAmount;

    @Schema(description = "Date of the next installment", example = "2025-04-01")
    private LocalDate nextInstallmentDate;

    @Schema(description = "Remaining amount to be paid off", example = "50000.00")
    private BigDecimal remainingDebt;

    @Schema(description = "Currency of the loan", example = "EUR")
    private CurrencyDto currency;

    @Schema(description = "Current status of the loan", example = "APPROVED")
    private LoanStatus status;

    @Schema(description = "Type of interest applied", example = "FIXED")
    private Loan.InterestType interestType;
}
