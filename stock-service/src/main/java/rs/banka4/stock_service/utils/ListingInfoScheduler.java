package rs.banka4.stock_service.utils;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import rs.banka4.stock_service.domain.exchanges.db.Exchange;
import rs.banka4.stock_service.domain.listing.db.Listing;
import rs.banka4.stock_service.domain.listing.db.ListingDailyPriceInfo;
import rs.banka4.stock_service.domain.security.Security;
import rs.banka4.stock_service.repositories.ListingDailyPriceInfoRepository;
import rs.banka4.stock_service.repositories.ListingRepository;
import rs.banka4.stock_service.repositories.SecurityRepository;
import rs.banka4.stock_service.runners.TestDataRunner;

@Profile("!test")
@Component
@RequiredArgsConstructor
public class ListingInfoScheduler {

    private final SecurityRepository securityRepository;

    private final ListingRepository listingRepository;

    private final ListingDailyPriceInfoRepository listingDailyPriceInfoRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(ListingInfoScheduler.class);

    // @Scheduled(cron = "0 1 0 * * *", zone = "Europe/Belgrade")
    @Scheduled(fixedDelayString = "#{1l * 60l * 1000l}")
    @Transactional
    public void scheduleListingInfoUpdates() {
        if (!TestDataRunner.finishedSeeding) {
            LOGGER.info(
                "Database not seeded yet. Skipping scheduled update of ListingDailyInfoPrices."
            );
            return;
        }

        LOGGER.info("Starting scheduleListingInfoUpdates");

        ZoneId zoneBelgrade = ZoneId.of("Europe/Belgrade");
        // ZonedDateTime nowBelgrade = ZonedDateTime.now(zoneBelgrade);
        ZonedDateTime nowBelgrade =
            ZonedDateTime.now(zoneBelgrade)
                .plusDays(1); // only for testing
        ZonedDateTime startOfToday = nowBelgrade.truncatedTo(ChronoUnit.DAYS);
        OffsetDateTime startOfYesterday =
            startOfToday.minusDays(1)
                .toInstant()
                .atOffset(ZoneOffset.UTC);
        OffsetDateTime endOfYesterday =
            startOfToday.toInstant()
                .atOffset(ZoneOffset.UTC);

        LOGGER.info("startOfYesterday " + startOfYesterday + " endOfYesterday " + endOfYesterday);

        List<ListingDailyPriceInfo> ldpis = new ArrayList<>();
        List<Security> securities = securityRepository.findAll();
        for (Security s : securities) {

            ListingDailyPriceInfo ldpi =
                makeYesterdaysListingDailyPriceInfo(s, startOfYesterday, endOfYesterday);
            if (ldpi == null) continue;

            Optional<List<ListingDailyPriceInfo>> todaysInfoOptional =
                listingDailyPriceInfoRepository.getListingDailyPriceInfoForDate(
                    s.getId(),
                    startOfYesterday.plusDays(1),
                    endOfYesterday.plusDays(1)
                );

            if (
                todaysInfoOptional.isPresent()
                    && !todaysInfoOptional.get()
                        .isEmpty()
            ) {
                LOGGER.error(
                    "There is already todays listing info! Count: "
                        + todaysInfoOptional.get()
                            .size()
                        + ". Security: "
                        + s.getTicker()
                );
                LOGGER.error("Deleting before new save.");
                listingDailyPriceInfoRepository.getListingDailyPriceInfoForDate(
                    s.getId(),
                    startOfYesterday.plusDays(1),
                    endOfYesterday.plusDays(1)
                );
            }

            ldpis.add(ldpi);
        }

        LOGGER.info(
            "Finished scheduleListingInfoUpdates for {} securities",
            ldpis.size() + " out of " + securities.size() + " securities in the system."
        );
        listingDailyPriceInfoRepository.saveAllAndFlush(ldpis);
    }

    public ListingDailyPriceInfo makeYesterdaysListingDailyPriceInfo(
        Security s,
        OffsetDateTime startOfYesterday,
        OffsetDateTime endOfYesterday
    ) {
        UUID securityId = s.getId();

        Optional<List<Listing>> yesterdaysListings =
            listingRepository.getAllSecurityListingsInAPeriod(
                securityId,
                startOfYesterday,
                endOfYesterday
            );

        if (
            !yesterdaysListings.isPresent()
                || yesterdaysListings.get()
                    .isEmpty()
        ) {
            LOGGER.error("No yesterday listings for " + s.getTicker());
            return null;
        }

        Exchange exchange =
            yesterdaysListings.get()
                .getFirst()
                .getExchange();
        BigDecimal askHigh = BigDecimal.ZERO;
        BigDecimal bigLow = BigDecimal.valueOf(9999999);
        for (Listing l : yesterdaysListings.get()) {
            if (
                l.getAsk()
                    .compareTo(askHigh)
                    > 0
            ) {
                askHigh = l.getAsk();
            }
            if (
                l.getBid()
                    .compareTo(bigLow)
                    < 0
            ) {
                bigLow = l.getBid();
            }
        }

        Listing lastListing =
            yesterdaysListings.get()
                .getLast(); // more than 1 shouldn't exist anyways

        Optional<List<ListingDailyPriceInfo>> yesterdayInfoOptional =
            listingDailyPriceInfoRepository.getListingDailyPriceInfoForDate(
                s.getId(),
                startOfYesterday,
                endOfYesterday
            );


        ListingDailyPriceInfo yesterdayInfo = null;
        if (
            yesterdayInfoOptional.isPresent()
                && !yesterdayInfoOptional.get()
                    .isEmpty()
        ) {
            yesterdayInfo =
                yesterdayInfoOptional.get()
                    .getLast();
        }

        BigDecimal lastPrice =
            lastListing.getAsk()
                .add(lastListing.getBid())
                .divide(BigDecimal.valueOf(2), RoundingMode.DOWN);
        BigDecimal yesterdayPrice;
        if (yesterdayInfo != null) yesterdayPrice = yesterdayInfo.getLastPrice();
        else {
            yesterdayPrice = lastPrice;
            // fake info change = 0 but the program will be able to start somewhere
        }
        return ListingDailyPriceInfo.builder()
            .askHigh(askHigh)
            .bigLow(bigLow)
            .security(s)
            .exchange(exchange)
            .date(OffsetDateTime.now())
            .lastPrice(lastPrice)
            .volume(
                yesterdaysListings.get()
                    .size() // This should be fixed once we have orders in the db
            )
            .change(
                yesterdayPrice.subtract(lastPrice)
                    .abs()
            )
            .build();

    }
}
