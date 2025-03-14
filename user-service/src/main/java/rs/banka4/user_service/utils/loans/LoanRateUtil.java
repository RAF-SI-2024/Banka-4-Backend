package rs.banka4.user_service.utils.loans;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import rs.banka4.user_service.domain.loan.db.BankMargin;
import rs.banka4.user_service.domain.loan.db.LoanType;
import rs.banka4.user_service.exceptions.loan.LoanTypeNotFound;
import rs.banka4.user_service.repositories.BankMarginRepository;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class LoanRateUtil {

    private final BankMarginRepository bankMarginRepository;

    /**
     * This is used for calculating new interest rates for variable rate loans
     *
     */
    public BigDecimal calculateInterestRate(BigDecimal referenceValue, LoanType loanType) {
        BankMargin bankMargin = bankMarginRepository.findBankMarginByType(loanType).orElseThrow(LoanTypeNotFound::new);
        return (referenceValue.add(bankMargin.getMargin())).divide(new BigDecimal(12),RoundingMode.HALF_UP);
    }


    /**
     *This calculates the amount that needs to be paid per month
     * For fixed rate loans it stays the same barring any penalties for delayed payments, but for variable rate loans
     * it should be called in conjunction with calculateInterestRate monthly in order to calculate for the installment
     */
    public BigDecimal calculateMonthly(BigDecimal loanAmount, BigDecimal monthlyInterestRate, BigInteger numberOfInstallments) {
        if (monthlyInterestRate.compareTo(BigDecimal.ZERO) == 0) {
            return loanAmount.divide(new BigDecimal(numberOfInstallments), 10, RoundingMode.HALF_UP);
        }

        BigDecimal one = BigDecimal.ONE;
        BigDecimal ratePlusOne = one.add(monthlyInterestRate);
        BigDecimal exponentiation = ratePlusOne.pow(numberOfInstallments.intValue());

        BigDecimal numerator = monthlyInterestRate.multiply(exponentiation);
        BigDecimal denominator = exponentiation.subtract(one);

        return loanAmount.multiply(numerator).divide(denominator, 10, RoundingMode.HALF_UP);
    }
}
