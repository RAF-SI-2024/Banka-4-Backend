package rs.banka4.stock_service.utils;

import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import rs.banka4.stock_service.domain.actuaries.db.ActuaryInfo;
import rs.banka4.stock_service.repositories.ActuaryRepository;
import rs.banka4.stock_service.service.impl.ActuaryServiceImpl;

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
            if (info.getUsedLimit() == null) continue;
            info.setUsedLimit(
                ActuaryServiceImpl.resetLimit(
                    info.getUsedLimit()
                        .getCurrency()
                )
            );
        }
        actuaryInfoRepository.saveAll(toReset);
    }
}
