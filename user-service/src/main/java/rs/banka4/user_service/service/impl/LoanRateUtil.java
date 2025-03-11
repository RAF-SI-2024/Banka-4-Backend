package rs.banka4.user_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import rs.banka4.user_service.repositories.LoanInstallmentRepository;
import rs.banka4.user_service.repositories.LoanRepository;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

@Component
public class LoanRateUtil {

    /**
     * Ovo se koristi kada se racuna nova kamatna stopa za varijabilne kredite
     * @return
     */
    public BigDecimal calculateInterestRate(BigDecimal referenceValue,BigDecimal bankMargin) {
        return (referenceValue.add(bankMargin)).divide(new BigDecimal(12),RoundingMode.HALF_UP);
    }


    /**
     * Ovo racuna kolicinu para koja se mora platiti na mjesecnom nivou
     * Za fiksan kredit ta suma uvjiek bude ista, za varijabilni ta suma se mijenja na mjesecnom nivou (valjda)
     * tada bi se na kraju svakog mjeseca pozvala metoda caculateInterestRate koja racuna novu kamatnu stopu za taj varijabilni kredit
     * za taj mjesec i racuna ratu za placanje za taj mjesec
     * (to sve preko cron joba se radi)
     * @param loanAmount
     * @param monthlyInterestRate
     * @param numberOfInstallments
     * @return
     */
    public BigDecimal calculateMonthly(BigInteger loanAmount, BigDecimal monthlyInterestRate, BigInteger numberOfInstallments) {
        if (monthlyInterestRate.compareTo(BigDecimal.ZERO) == 0) {
            return new BigDecimal(loanAmount).divide(new BigDecimal(numberOfInstallments), 10, RoundingMode.HALF_UP);
        }

        BigDecimal one = BigDecimal.ONE;
        BigDecimal ratePlusOne = one.add(monthlyInterestRate);
        BigDecimal exponentiation = ratePlusOne.pow(numberOfInstallments.intValue());

        BigDecimal numerator = monthlyInterestRate.multiply(exponentiation);
        BigDecimal denominator = exponentiation.subtract(one);

        return new BigDecimal(loanAmount).multiply(numerator).divide(denominator, 10, RoundingMode.HALF_UP);
    }









}
