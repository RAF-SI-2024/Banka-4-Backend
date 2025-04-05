package rs.banka4.stock_service.utils;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import rs.banka4.stock_service.domain.actuaries.db.ActuaryInfo;
import rs.banka4.stock_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.stock_service.repositories.ActuaryRepository;

@Component
@RequiredArgsConstructor
public class LimitResetScheduler {

    private final ActuaryRepository actuaryInfoRepository;

    /**
     * <p>
     * Scheduled task that resets the {@code usedLimit} to 10,000 RSD for all agents (who require
     * approval) (i.e., {@code needApproval == true}).
     * </p>
     * <p>
     * This method runs daily at midnight and updates the database in a single transaction.
     * </p>
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void resetUsedLimit() {
        List<ActuaryInfo> toReset = actuaryInfoRepository.findByNeedApprovalTrue();

        for (ActuaryInfo info : toReset) {
            MonetaryAmount monetaryAmount = info.getUsedLimit();
            monetaryAmount.setAmount(BigDecimal.valueOf(10000));
            info.setUsedLimit(monetaryAmount);
        }

        actuaryInfoRepository.saveAll(toReset);
    }
}
