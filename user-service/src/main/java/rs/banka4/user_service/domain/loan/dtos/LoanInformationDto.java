package rs.banka4.user_service.domain.loan.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import rs.banka4.user_service.domain.currency.dtos.CurrencyDto;
import rs.banka4.user_service.domain.loan.db.LoanType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "DTO for loan information")
public record LoanInformationDto(

        @Schema(description = "Unique identifier for the loan", example = "1234567")
        Long loanNumber,

        @Schema(description = "Type of loan", example = "CASH")
        LoanType type,

        @Schema(description = "Total amount of the loan", example = "100000.00")
        BigDecimal amount,

        @Schema(description = "Number of months/installments for repayment", example = "120")
        Integer repaymentPeriod,

        @Schema(description = "Interest rate at the time of credit creation", example = "4.5")
        BigDecimal interestRate,

        @Schema(description = "Current effective interest rate", example = "5.0")
        BigDecimal effectiveInterestRate,

        @Schema(description = "Date the loan agreement was signed", example = "2021-05-15")
        LocalDate agreementDate,

        @Schema(description = "Date by which the loan should be fully paid off", example = "2031-05-15")
        LocalDate dueDate,

        @Schema(description = "Amount of the next installment to be paid", example = "950.00")
        BigDecimal nextInstallmentAmount,

        @Schema(description = "Date of the next installment", example = "2025-04-01")
        LocalDate nextInstallmentDate,

        @Schema(description = "Remaining amount to be paid off", example = "50000.00")
        BigDecimal remainingDebt,

        @Schema(description = "Currency of the loan", example = "EUR")
        CurrencyDto currency

) { }