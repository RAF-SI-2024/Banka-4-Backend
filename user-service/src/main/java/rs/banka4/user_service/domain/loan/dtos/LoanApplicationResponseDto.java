package rs.banka4.user_service.domain.loan.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import rs.banka4.user_service.domain.currency.db.Currency;
import rs.banka4.user_service.domain.loan.db.Loan;
import rs.banka4.user_service.domain.loan.db.LoanType;

@Schema(description = "DTO for loan application information")
public record LoanApplicationResponseDto(
    @Schema(
        description = "Unique identifier for the loan",
        example = "1234567"
    ) Long loanNumber,

    @Schema(
        description = "Type of loan",
        example = "CASH"
    ) LoanType loanType,

    @Schema(
        description = "Total amount of the loan",
        example = "10000.00"
    ) BigDecimal amount,

    @Schema(
        description = "Currency of the loan",
        example = "EUR"
    ) Currency currency,

    @Schema(
        description = "Purpose of the loan",
        example = "Education"
    ) String purposeOfLoan,

    @Schema(
        description = "Applicant's monthly income",
        example = "2500"
    ) BigDecimal monthlyIncome,

    @Schema(
        description = "Employment status of the applicant",
        example = "Permanent"
    ) String employmentStatus,

    @Schema(
        description = "Duration of employment in years",
        example = "5"
    ) Integer employmentPeriod,

    @Schema(
        description = "Repayment period in months",
        example = "60"
    ) Integer repaymentPeriod,

    @Schema(
        description = "Applicant's contact phoneNumber number",
        example = "+381641234567"
    ) String contactPhone,

    @Schema(
        description = "Applicant's bank account number",
        example = "35123456789012345678"
    ) String accountNumber,

    @Schema(
        description = "Chosen interest rate",
        example = "FIXED"
    ) Loan.InterestType interestType
) {
}
