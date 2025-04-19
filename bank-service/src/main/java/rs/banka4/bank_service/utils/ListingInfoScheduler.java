package rs.banka4.bank_service.utils;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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
import rs.banka4.bank_service.domain.exchanges.db.Exchange;
import rs.banka4.bank_service.domain.listing.db.Listing;
import rs.banka4.bank_service.domain.listing.db.ListingDailyPriceInfo;
import rs.banka4.bank_service.domain.security.Security;
import rs.banka4.bank_service.repositories.ListingDailyPriceInfoRepository;
import rs.banka4.bank_service.repositories.ListingRepository;
import rs.banka4.bank_service.repositories.SecurityRepository;
import rs.banka4.bank_service.runners.ListingsDataRunner;

@Profile("!test")
@Component
@RequiredArgsConstructor
public class ListingInfoScheduler {

    private final SecurityRepository securityRepository;

    private final ListingRepository listingRepository;

    private final ListingDailyPriceInfoRepository listingDailyPriceInfoRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(ListingInfoScheduler.class);

    // @Scheduled(fixedDelayString = "#{7l * 60l * 1000l}")
    @Scheduled(
        cron = "0 1 0 * * *",
        zone = "Europe/Belgrade"
    )
    @Transactional
    public void scheduleListingInfoUpdates() {
        if (!ListingsDataRunner.finishedSeeding) {
            LOGGER.info(
                "Database not seeded yet. Skipping scheduled update of ListingDailyInfoPrices."
            );
            return;
        }

        LOGGER.info("Starting scheduleListingInfoUpdates");

        ZoneId zoneBelgrade = ZoneId.of("Europe/Belgrade");
        ZonedDateTime nowBelgrade = ZonedDateTime.now(zoneBelgrade);
        // ZonedDateTime nowBelgrade = ZonedDateTime.now(zoneBelgrade) .plusDays(1); // only for
        // testing
        ZonedDateTime startOfToday = nowBelgrade.truncatedTo(ChronoUnit.DAYS);

        OffsetDateTime startOfYesterday =
            startOfToday.minusDays(1)
                .toOffsetDateTime();
        OffsetDateTime endOfYesterday = startOfToday.toOffsetDateTime();

        LOGGER.info("startOfYesterday " + startOfYesterday + " endOfYesterday " + endOfYesterday);

        List<ListingDailyPriceInfo> ldpis = new ArrayList<>();
        List<Security> securities = securityRepository.findAll();
        for (Security s : securities) {

            ListingDailyPriceInfo ldpi =
                makeYesterdaysListingDailyPriceInfo(s, startOfYesterday, endOfYesterday);
            if (ldpi == null) continue;

            Optional<List<ListingDailyPriceInfo>> yesterdaysInfoOptional =
                listingDailyPriceInfoRepository.getListingDailyPriceInfoForDate(
                    s.getId(),
                    startOfYesterday,
                    endOfYesterday
                );

            if (
                yesterdaysInfoOptional.isPresent()
                    && !yesterdaysInfoOptional.get()
                        .isEmpty()
            ) {
                LOGGER.error(
                    "There is already yesterdays listing info! Count: "
                        + yesterdaysInfoOptional.get()
                            .size()
                        + ". Security: "
                        + s.getTicker()
                );
                LOGGER.error("Deleting before new save.");
                listingDailyPriceInfoRepository.deleteListingDailyPriceInfoForDate(
                    s.getId(),
                    startOfYesterday,
                    endOfYesterday
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

        Optional<List<ListingDailyPriceInfo>> twoDaysAgoInfoOpt =
            listingDailyPriceInfoRepository.getListingDailyPriceInfoForDate(
                s.getId(),
                startOfYesterday.minusDays(1),
                endOfYesterday.minusDays(1)
            );


        ListingDailyPriceInfo twoDaysAgoInfo = null;
        if (
            twoDaysAgoInfoOpt.isPresent()
                && !twoDaysAgoInfoOpt.get()
                    .isEmpty()
        ) {
            twoDaysAgoInfo =
                twoDaysAgoInfoOpt.get()
                    .getLast();
        }

        BigDecimal lastPrice =
            lastListing.getAsk()
                .add(lastListing.getBid())
                .divide(BigDecimal.valueOf(2), RoundingMode.DOWN);
        BigDecimal twoDaysAgoPrice;
        if (twoDaysAgoInfo != null) twoDaysAgoPrice = twoDaysAgoInfo.getLastPrice();
        else {
            twoDaysAgoPrice = lastPrice;
            // fake info change = 0 but the program will be able to start somewhere
        }
        return ListingDailyPriceInfo.builder()
            .askHigh(askHigh)
            .bigLow(bigLow)
            .security(s)
            .exchange(exchange)
            .date(endOfYesterday.minusHours(12))
            .lastPrice(lastPrice)
            .volume(
                yesterdaysListings.get()
                    .size() // This should be fixed once we have orders in the db
            )
            .change(
                twoDaysAgoPrice.subtract(lastPrice)
                    .abs()
            )
            .build();

    }
}
