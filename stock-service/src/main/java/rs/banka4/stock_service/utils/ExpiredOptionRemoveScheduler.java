package rs.banka4.stock_service.utils;

import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import rs.banka4.stock_service.repositories.OptionsRepository;

@Component
@RequiredArgsConstructor
public class ExpiredOptionRemoveScheduler {
    private final OptionsRepository optionsRepository;
    private static final Logger LOGGER =
        LoggerFactory.getLogger(ExpiredOptionRemoveScheduler.class);


    @Scheduled(cron = "0 */30 * * * *")
    @Transactional
    public void removeExpiredOptions() {
        // Running every 30 mins because I'm not sure if its UTC or CET for cronjob It can't delete
        // it twice anyway
        LOGGER.info("Starting to clean expired options...");
        long beforeCount = optionsRepository.count();
        optionsRepository.deleteOldOptions(OffsetDateTime.now(ZoneId.of("Europe/Belgrade")));
        // to test on real data add plusDays(28) to go in future
        long afterCount = optionsRepository.count();

        LOGGER.info(
            "Expired options cleanup complete. Options before: {}, after: {}, removed: {}",
            beforeCount,
            afterCount,
            beforeCount - afterCount
        );
    }
}
