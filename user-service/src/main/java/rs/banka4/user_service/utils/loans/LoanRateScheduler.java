package rs.banka4.user_service.utils.loans;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.domain.loan.db.Loan;
import rs.banka4.user_service.domain.loan.db.LoanStatus;
import rs.banka4.user_service.repositories.LoanRepository;

/**
 * Service responsible for managing and updating loan interest rates. It performs scheduled tasks
 * related to loan interest rates, including: - Applying variable interest rates to approved loans -
 * Updating the interest rate variant every month
 */
@Service
@RequiredArgsConstructor
public class LoanRateScheduler {

    /**
     * A static variable representing the interest rate variant applied to loans. This value is
     * randomly generated within a range of -1.5% to 1.5% and is updated on the first day of every
     * month. It affects variable interest rate loans.
     */
    @Getter
    private static BigDecimal interestRateVariant = generateRandomPercentage();
    private final LoanRepository loanRepository;


    /**
     * Applies variable interest rates to all approved loans on the first day of each month. It
     * updates the fixed rate of loans based on the base interest rate and a randomly generated
     * variant.
     */
    @Scheduled(cron = "0 5 0 1 * *") // Cron expression for the first day of every month at midnight
    public void applyVariableRateToAllVariableLoans() {
        var loans =
            loanRepository.findByInterestTypeAndStatus(
                Loan.InterestType.VARIABLE,
                LoanStatus.APPROVED
            );
        if (loans.isEmpty()) {
            return;
        }
        loans.get()
            .forEach(loan -> {
                loan.getInterestRate()
                    .setFixedRate(
                        loan.getBaseInterestRate()
                            .add(interestRateVariant)
                    );
            });
        loanRepository.saveAll(loans.get());
    }

    /**
     * Generates a random percentage value to use as an interest rate variant. The value is within
     * the range of -1.5% to 1.5%.
     *
     * @return A random percentage value rounded to two decimal places.
     */
    private static BigDecimal generateRandomPercentage() {
        Random random = new Random();
        double randomValue = -1.5 + (1.5 - (-1.5)) * random.nextDouble();
        return new BigDecimal(randomValue).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Updates the interest rate variant on the first day of every month. The new variant is
     * generated randomly within a specified range.
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void updateInterestRateVariant() {
        interestRateVariant = generateRandomPercentage();
    }
}
