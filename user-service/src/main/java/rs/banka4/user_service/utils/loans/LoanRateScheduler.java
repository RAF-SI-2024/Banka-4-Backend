package rs.banka4.user_service.utils.loans;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.domain.loan.db.Loan;
import rs.banka4.user_service.domain.loan.db.LoanStatus;
import rs.banka4.user_service.repositories.LoanRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class LoanRateScheduler {
    @Getter
    private static BigDecimal interestRateVariant = generateRandomPercentage();
    private final LoanRepository loanRepository;

    @Scheduled(cron = "0 5 0 1 * *")  // Cron expression for the first day of every month at midnight
    public void applyVariableRateToAllVariableLoans(){
        var loans = loanRepository.findByInterestTypeAndStatus(Loan.InterestType.VARIABLE,LoanStatus.APPROVED);
        if(loans.isEmpty()){
            return;
        }
        loans.get().forEach(loan -> {
            loan.getInterestRate().setFixedRate(loan.getBaseInterestRate().add(interestRateVariant));
        });
        loanRepository.saveAll(loans.get());
    }

    private static BigDecimal generateRandomPercentage() {
        Random random = new Random();
        double randomValue = -1.5 + (1.5 - (-1.5)) * random.nextDouble();
        return new BigDecimal(randomValue).setScale(2, RoundingMode.HALF_UP);
    }

    @Scheduled(cron = "0 0 0 1 * ?")
    public void updateInterestRateVariant() {
        interestRateVariant = generateRandomPercentage();
    }
}
