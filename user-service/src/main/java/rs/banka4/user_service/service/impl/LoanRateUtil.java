package rs.banka4.user_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import rs.banka4.user_service.domain.loan.db.BankMargin;
import rs.banka4.user_service.domain.loan.db.LoanType;
import rs.banka4.user_service.exceptions.loan.LoanTypeNotFound;
import rs.banka4.user_service.repositories.BankMarginRepositroy;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class LoanRateUtil {

    private final BankMarginRepositroy bankMarginRepositroy;
    private static BigDecimal interestRateVariant;

    /**
     * Ovo se koristi kada se racuna nova kamatna stopa za varijabilne kredite
     * @return
     */
    public BigDecimal calculateInterestRate(BigDecimal referenceValue, LoanType loanType) {
        BankMargin bankMargin = bankMarginRepositroy.findBankMarginByType(loanType).orElseThrow(LoanTypeNotFound::new);
        return (referenceValue.add(bankMargin.getMargin())).divide(new BigDecimal(12),RoundingMode.HALF_UP);
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

    /**
     * This method simulates a random change in interest rate for variable rate loans
     */
    private static BigDecimal generateRandomPercentage() {
        Random random = new Random();

        // Generate a random double between -1.5 and 1.5
        double randomValue = -1.5 + (1.5 - (-1.5)) * random.nextDouble();

        // Convert it to a BigDecimal and round it to two decimal places (to represent percentage accurately)
        return new BigDecimal(randomValue).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * This cron job updates the interest rate variant at midnight on the first day of every month
     */
    @Scheduled(cron = "0 0 0 1 * ?")  // Cron expression for the first day of every month at midnight
    public void updateInterestRateVariant() {
        interestRateVariant = generateRandomPercentage();
        System.out.println("Updated interest rate variant: " + interestRateVariant);
    }

}
