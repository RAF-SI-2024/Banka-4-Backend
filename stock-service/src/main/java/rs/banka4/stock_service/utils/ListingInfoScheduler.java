package rs.banka4.stock_service.utils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Limit;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Profile("!test")
@Component
@RequiredArgsConstructor
public class ListingInfoScheduler {

    private final SecurityRepository securityRepository;

    private final ListingRepository listingRepository;

    private final ListingDailyPriceInfoRepository listingDailyPriceInfoRepository;

    private static final Logger LOGGER =
        LoggerFactory.getLogger(ListingInfoScheduler.class);

    @Scheduled(cron = "0 1 0 * * *", zone = "Europe/Belgrade")
    //@Scheduled(fixedDelayString = "#{2l * 60l * 1000l}")
    @Transactional
    public void scheduleListingInfoUpdates() {
        if (!TestDataRunner.finishedSeeding) {
            System.out.println(
                "Database not seeded yet. Skipping scheduled update of listings and options."
            );
            return;
        }
        List<Security> securities = securityRepository.findAll();
        for(Security s : securities){
            Optional<List<Listing>> listings = listingRepository.getAllBySecurity(s.getId());

            if(listings.isPresent()){
                OffsetDateTime yesterdaysDate = listings.get().getFirst().getLastRefresh();
                Exchange exchange = listings.get().getFirst().getExchange();
                BigDecimal askHigh = BigDecimal.ZERO;
                BigDecimal bigLow = BigDecimal.valueOf(9999999);
                for(Listing l : listings.get()){
                    if(l.getLastRefresh().getDayOfMonth() != yesterdaysDate.getDayOfMonth())
                        break;
                    if(l.getAsk().compareTo(askHigh) > 0){
                        askHigh = l.getAsk();
                    }
                    if(l.getBid().compareTo(bigLow) < 0){
                        bigLow = l.getBid();
                    }
                }

                Optional<ListingDailyPriceInfo> yesterdayInfo = listingDailyPriceInfoRepository.getYesterdayListingDailyPriceInfo(s.getId(), Limit.of(1));

                Listing l = listings.get().getLast();

                BigDecimal lastPrice = l.getAsk().add(l.getBid()).divide(BigDecimal.valueOf(2), RoundingMode.DOWN);
                BigDecimal yesterdayPrice;
                if(yesterdayInfo.isPresent())
                     yesterdayPrice = yesterdayInfo.get().getLastPrice();
                else{
                    LOGGER.error("THERE IS NO YESTERDAYS LISTING INFO!");
                    return;
                }
                ListingDailyPriceInfo ldpi = ListingDailyPriceInfo
                    .builder()
                    .askHigh(askHigh)
                    .bigLow(bigLow)
                    .security(s)
                    .exchange(exchange)
                    .date(OffsetDateTime.now())
                    .lastPrice(lastPrice)
                    .volume(listings.get().size())
                    .change(yesterdayPrice.subtract(lastPrice).abs())
                    .build();

                listingDailyPriceInfoRepository.save(ldpi);
            }
        }
    }
}
