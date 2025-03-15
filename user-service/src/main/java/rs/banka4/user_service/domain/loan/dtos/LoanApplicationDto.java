package rs.banka4.user_service.domain.loan.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import rs.banka4.user_service.domain.currency.db.Currency;
import rs.banka4.user_service.domain.loan.db.Loan;
import rs.banka4.user_service.domain.loan.db.LoanType;

import java.math.BigDecimal;

@Schema(description = "DTO for loan application information")
public record LoanApplicationDto(

        @NotNull
        @Schema(description = "Type of loan", example = "CASH")
        LoanType loanType,

        @NotNull
        @Schema(description = "Total amount of the loan", example = "10000.00")
        BigDecimal amount,

        @NotNull
        @Schema(description = "Currency of the loan", example = "EUR")
        Currency.Code currency,

        @NotBlank
        @Schema(description = "Purpose of the loan", example = "Education")
        String purposeOfLoan,

        @NotNull
        @Schema(description = "Applicant's monthly income", example = "2500")
        BigDecimal monthlyIncome,

        @NotBlank
        @Schema(description = "Employment status of the applicant", example = "Permanent")
        String employmentStatus,

        @NotNull
        @Schema(description = "Duration of employment in years", example = "5")
        Integer employmentPeriod,

        @NotNull
        @Schema(description = "Repayment period in months", example = "60")
        Integer repaymentPeriod,

        @NotBlank
        @Schema(description = "Applicant's contact phone number", example = "+381641234567")
        String contactPhone,

        @NotBlank
        @Schema(description = "Applicant's bank account number", example = "35123456789012345678")
        String accountNumber,

        @NotNull
        @Schema(description = "Chosen interest rate", example = "FIXED")
        Loan.InterestType interestType
) {
}