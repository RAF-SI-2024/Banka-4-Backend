package rs.banka4.stock_service.runners;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import rs.banka4.stock_service.domain.exchanges.db.Exchange;
import rs.banka4.stock_service.domain.security.forex.db.CurrencyCode;
import rs.banka4.stock_service.domain.security.forex.db.ForexLiquidity;
import rs.banka4.stock_service.domain.security.forex.db.ForexPair;
import rs.banka4.stock_service.domain.security.future.db.Future;
import rs.banka4.stock_service.domain.security.future.db.UnitName;
import rs.banka4.stock_service.domain.security.stock.db.Stock;
import rs.banka4.stock_service.repositories.*;

@Profile({
    "dev"
})
@Component
@RequiredArgsConstructor
public class TestDataRunner implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestDataRunner.class);

    private final ForexRepository forexPairRepository;
    private final StockRepository stockRepository;
    private final FutureRepository futureRepository;
    private final ListingRepository listingRepository;
    private final ExchangeRepository exchangeRepository;
    private final ListingDailyPriceInfoRepository listingDailyPriceInfoRepository;

    @Override
    public void run(String... args) {
        // added comment to trigger style check on git after it crashed
        if (stockRepository.count() == 0) {
            stocksSeeder();
        } else {
            LOGGER.info("Not reseeding stockRepository, data already exists");
        }

        if (forexPairRepository.count() == 0) {
            forexPairsSeeder();
        } else {
            LOGGER.info("Not reseeding forexPairRepository, data already exists");
        }

        if (futureRepository.count() == 0) {
            futuresSeeder();
        } else {
            LOGGER.info("Not reseeding futureRepository, data already exists");
        }

        if (exchangeRepository.count() == 0) {
            exchangesSeeder();
        } else {
            LOGGER.info("Not reseeding exchangeRepository, data already exists");
        }
    }

    private void forexPairsSeeder() {
        ForexLiquidity[] forexLiquidities = ForexLiquidity.values();
        int i = 0;
        List<ForexPair> forexPairs = new ArrayList<>();
        for (CurrencyCode currencyCode1 : CurrencyCode.values()) {
            for (CurrencyCode currencyCode2 : CurrencyCode.values()) {
                if (currencyCode1.equals(currencyCode2)) {
                    continue;
                }

                forexPairs.add(
                    ForexPair.builder()
                        .baseCurrency(currencyCode1)
                        .quoteCurrency(currencyCode2)
                        .liquidity(forexLiquidities[i++])
                        .name(currencyCode1 + "/" + currencyCode2)
                        .build()
                );
                i = i % 3;
            }
        }
        forexPairRepository.saveAll(forexPairs);
        LOGGER.info("forex pairs seeded successfully");
    }

    private void stocksSeeder() {
        List<Stock> stocks =
            List.of(
                Stock.builder()
                    .name("Apple")
                    .outstandingShares(16000000)
                    .dividendYield(new BigDecimal("0.005"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("Microsoft")
                    .outstandingShares(7500000000L)
                    .dividendYield(new BigDecimal("0.008"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("Amazon")
                    .outstandingShares(1020000000)
                    .dividendYield(new BigDecimal("0"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("Tesla")
                    .outstandingShares(3200000000L)
                    .dividendYield(new BigDecimal("0"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("Nvidia")
                    .outstandingShares(2500000000L)
                    .dividendYield(new BigDecimal("0.002"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("Google (Alphabet)")
                    .outstandingShares(600000000)
                    .dividendYield(new BigDecimal("0"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("Meta (Facebook)")
                    .outstandingShares(2700000000L)
                    .dividendYield(new BigDecimal("0"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("Berkshire Hathaway")
                    .outstandingShares(2200000000L)
                    .dividendYield(new BigDecimal("0"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("Johnson & Johnson")
                    .outstandingShares(2600000000L)
                    .dividendYield(new BigDecimal("0.025"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("JPMorgan Chase")
                    .outstandingShares(2900000000L)
                    .dividendYield(new BigDecimal("0.029"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("Visa")
                    .outstandingShares(2200000000L)
                    .dividendYield(new BigDecimal("0.008"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("Procter & Gamble")
                    .outstandingShares(2500000000L)
                    .dividendYield(new BigDecimal("0.026"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("UnitedHealth Group")
                    .outstandingShares(900000000)
                    .dividendYield(new BigDecimal("0.016"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("ExxonMobil")
                    .outstandingShares(4000000000L)
                    .dividendYield(new BigDecimal("0.031"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("Walmart")
                    .outstandingShares(2800000000L)
                    .dividendYield(new BigDecimal("0.014"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("Pfizer")
                    .outstandingShares(5600000000L)
                    .dividendYield(new BigDecimal("0.046"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("Chevron")
                    .outstandingShares(1900000000L)
                    .dividendYield(new BigDecimal("0.041"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("Coca-Cola")
                    .outstandingShares(4300000000L)
                    .dividendYield(new BigDecimal("0.029"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("Disney")
                    .outstandingShares(1800000000L)
                    .dividendYield(new BigDecimal("0"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("PepsiCo")
                    .outstandingShares(1400000000L)
                    .dividendYield(new BigDecimal("0.028"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("Intel")
                    .outstandingShares(4200000000L)
                    .dividendYield(new BigDecimal("0.016"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("McDonald's")
                    .outstandingShares(730000000)
                    .dividendYield(new BigDecimal("0.022"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("IBM")
                    .outstandingShares(900000000)
                    .dividendYield(new BigDecimal("0.048"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("Netflix")
                    .outstandingShares(450000000)
                    .dividendYield(new BigDecimal("0"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("AMD")
                    .outstandingShares(1600000000L)
                    .dividendYield(new BigDecimal("0"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("Qualcomm")
                    .outstandingShares(1200000000L)
                    .dividendYield(new BigDecimal("0.027"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("Boeing")
                    .outstandingShares(600000000)
                    .dividendYield(new BigDecimal("0"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("Goldman Sachs")
                    .outstandingShares(340000000)
                    .dividendYield(new BigDecimal("0.023"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("Ford")
                    .outstandingShares(4000000000L)
                    .dividendYield(new BigDecimal("0.036"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("General Motors")
                    .outstandingShares(1400000000L)
                    .dividendYield(new BigDecimal("0.033"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("AT&T")
                    .outstandingShares(7100000000L)
                    .dividendYield(new BigDecimal("0.057"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("Verizon")
                    .outstandingShares(4200000000L)
                    .dividendYield(new BigDecimal("0.064"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("Starbucks")
                    .outstandingShares(1200000000L)
                    .dividendYield(new BigDecimal("0.023"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("3M")
                    .outstandingShares(550000000)
                    .dividendYield(new BigDecimal("0.057"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("AbbVie")
                    .outstandingShares(1800000000L)
                    .dividendYield(new BigDecimal("0.040"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("Nike")
                    .outstandingShares(1600000000L)
                    .dividendYield(new BigDecimal("0.012"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("Tesla")
                    .outstandingShares(3200000000L)
                    .dividendYield(new BigDecimal("0"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("Cisco")
                    .outstandingShares(4100000000L)
                    .dividendYield(new BigDecimal("0.028"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("American Express")
                    .outstandingShares(800000000)
                    .dividendYield(new BigDecimal("0.015"))
                    .createdAt(OffsetDateTime.now())
                    .build(),
                Stock.builder()
                    .name("PayPal")
                    .outstandingShares(1100000000L)
                    .dividendYield(new BigDecimal("0"))
                    .createdAt(OffsetDateTime.now())
                    .build()
            );
        stockRepository.saveAll(stocks);
        LOGGER.info("stocks seeded successfully");
    }

    private void futuresSeeder() {
        List<Future> futures =
            List.of(
                Future.builder()
                    .name("Corn")
                    .contractSize(5000)
                    .contractUnit(UnitName.BUSHEL)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusYears(1)
                    )
                    .build(),
                Future.builder()
                    .name("Soybean")
                    .contractSize(5000)
                    .contractUnit(UnitName.BUSHEL)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusYears(2)
                    )
                    .build(),
                Future.builder()
                    .name("Soybean Oil")
                    .contractSize(60000)
                    .contractUnit(UnitName.POUND)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusYears(1)
                    )
                    .build(),
                Future.builder()
                    .name("Soybean Meal")
                    .contractSize(180000)
                    .contractUnit(UnitName.POUND)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(9)
                    )
                    .build(),
                Future.builder()
                    .name("Chicago Wheat")
                    .contractSize(5000)
                    .contractUnit(UnitName.BUSHEL)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(10)
                    )
                    .build(),
                Future.builder()
                    .name("Live Cattle")
                    .contractSize(40000)
                    .contractUnit(UnitName.POUND)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(11)
                    )
                    .build(),
                Future.builder()
                    .name("Wheat")
                    .contractSize(5000)
                    .contractUnit(UnitName.BUSHEL)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(7)
                    )
                    .build(),
                Future.builder()
                    .name("Feeder Cattle")
                    .contractSize(50000)
                    .contractUnit(UnitName.POUND)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(8)
                    )
                    .build(),
                Future.builder()
                    .name("Lean Hog")
                    .contractSize(40000)
                    .contractUnit(UnitName.POUND)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusYears(3)
                    )
                    .build(),
                Future.builder()
                    .name("Pork Cutout")
                    .contractSize(40000)
                    .contractUnit(UnitName.POUND)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(6)
                    )
                    .build(),
                Future.builder()
                    .name("Nonfat Dry Milk")
                    .contractSize(44000)
                    .contractUnit(UnitName.POUND)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(3)
                    )
                    .build(),
                Future.builder()
                    .name("Class 3 Milk")
                    .contractSize(200000)
                    .contractUnit(UnitName.POUND)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(11)
                    )
                    .build(),
                Future.builder()
                    .name("Class 4 Milk")
                    .contractSize(200000)
                    .contractUnit(UnitName.POUND)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(3)
                    )
                    .build(),
                Future.builder()
                    .name("Cash-Settled Butter")
                    .contractSize(20000)
                    .contractUnit(UnitName.POUND)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(9)
                    )
                    .build(),
                Future.builder()
                    .name("Cash-Settled Cheese")
                    .contractSize(20000)
                    .contractUnit(UnitName.POUND)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(7)
                    )
                    .build(),
                Future.builder()
                    .name("Block Cheese")
                    .contractSize(2000)
                    .contractUnit(UnitName.POUND)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(9)
                    )
                    .build(),
                Future.builder()
                    .name("Oats")
                    .contractSize(5000)
                    .contractUnit(UnitName.BUSHEL)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(4)
                    )
                    .build(),
                Future.builder()
                    .name("Rough Rice")
                    .contractSize(180000)
                    .contractUnit(UnitName.POUND)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(33)
                    )
                    .build(),
                Future.builder()
                    .name("Urea")
                    .contractSize(200000)
                    .contractUnit(UnitName.POUND)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(13)
                    )
                    .build(),
                Future.builder()
                    .name("Crude Oil")
                    .contractSize(1000)
                    .contractUnit(UnitName.BARREL)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(13)
                    )
                    .build(),
                Future.builder()
                    .name("Natural Gas")
                    .contractSize(10000)
                    .contractUnit(UnitName.MMBTU)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(7)
                    )
                    .build(),
                Future.builder()
                    .name("Gasoline")
                    .contractSize(42000)
                    .contractUnit(UnitName.GALLON)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(9)
                    )
                    .build(),
                Future.builder()
                    .name("E-Mini Crude Oil")
                    .contractSize(500)
                    .contractUnit(UnitName.BARREL)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(12)
                    )
                    .build(),
                Future.builder()
                    .name("NY Harbor ULSD")
                    .contractSize(42000)
                    .contractUnit(UnitName.GALLON)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(10)
                    )
                    .build(),
                Future.builder()
                    .name("Micro Crude Oil")
                    .contractSize(100)
                    .contractUnit(UnitName.BARREL)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(8)
                    )
                    .build(),
                Future.builder()
                    .name("Henry Hub Natural Gas")
                    .contractSize(10000)
                    .contractUnit(UnitName.MMBTU)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(6)
                    )
                    .build(),
                Future.builder()
                    .name("Buckeye Jet Fuel")
                    .contractSize(42000)
                    .contractUnit(UnitName.GALLON)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(14)
                    )
                    .build(),
                Future.builder()
                    .name("Gold")
                    .contractSize(100)
                    .contractUnit(UnitName.TROY_OUNCE)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(15)
                    )
                    .build(),
                Future.builder()
                    .name("Silver")
                    .contractSize(5000)
                    .contractUnit(UnitName.TROY_OUNCE)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(16)
                    )
                    .build(),
                Future.builder()
                    .name("Platinum")
                    .contractSize(50)
                    .contractUnit(UnitName.TROY_OUNCE)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(17)
                    )
                    .build(),
                Future.builder()
                    .name("Copper")
                    .contractSize(25000)
                    .contractUnit(UnitName.POUND)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(18)
                    )
                    .build(),
                Future.builder()
                    .name("Aluminum")
                    .contractSize(50000)
                    .contractUnit(UnitName.POUND)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(19)
                    )
                    .build(),
                Future.builder()
                    .name("E-Mini Copper")
                    .contractSize(12500)
                    .contractUnit(UnitName.POUND)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(20)
                    )
                    .build(),
                Future.builder()
                    .name("Copper Mini")
                    .contractSize(12500)
                    .contractUnit(UnitName.POUND)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(21)
                    )
                    .build(),
                Future.builder()
                    .name("Silver Mini")
                    .contractSize(1000)
                    .contractUnit(UnitName.TROY_OUNCE)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(22)
                    )
                    .build(),
                Future.builder()
                    .name("Platinum Mini")
                    .contractSize(10)
                    .contractUnit(UnitName.TROY_OUNCE)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(23)
                    )
                    .build(),
                Future.builder()
                    .name("Gold Options")
                    .contractSize(100)
                    .contractUnit(UnitName.TROY_OUNCE)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(24)
                    )
                    .build(),
                Future.builder()
                    .name("Silver Options")
                    .contractSize(5000)
                    .contractUnit(UnitName.TROY_OUNCE)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(25)
                    )
                    .build(),
                Future.builder()
                    .name("Palladium Options")
                    .contractSize(100)
                    .contractUnit(UnitName.TROY_OUNCE)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(26)
                    )
                    .build(),
                Future.builder()
                    .name("Cotton")
                    .contractSize(50000)
                    .contractUnit(UnitName.POUND)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(27)
                    )
                    .build(),
                Future.builder()
                    .name("Coffee")
                    .contractSize(37500)
                    .contractUnit(UnitName.POUND)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(28)
                    )
                    .build(),
                Future.builder()
                    .name("Sugar")
                    .contractSize(112000)
                    .contractUnit(UnitName.POUND)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(29)
                    )
                    .build(),
                Future.builder()
                    .name("Cocoa")
                    .contractSize(10)
                    .contractUnit(UnitName.METRIC_TON)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(30)
                    )
                    .build(),
                Future.builder()
                    .name("Orange Juice")
                    .contractSize(15000)
                    .contractUnit(UnitName.POUND)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(31)
                    )
                    .build(),
                Future.builder()
                    .name("Lumber Options")
                    .contractSize(1100)
                    .contractUnit(UnitName.BOARD_FEET)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(32)
                    )
                    .build(),
                Future.builder()
                    .name("Lean Hog Options")
                    .contractSize(40000)
                    .contractUnit(UnitName.POUND)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(33)
                    )
                    .build(),
                Future.builder()
                    .name("Live Cattle Options")
                    .contractSize(40000)
                    .contractUnit(UnitName.POUND)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(34)
                    )
                    .build(),
                Future.builder()
                    .name("Feeder Cattle Options")
                    .contractSize(50000)
                    .contractUnit(UnitName.POUND)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(35)
                    )
                    .build(),
                Future.builder()
                    .name("Butter Options")
                    .contractSize(20000)
                    .contractUnit(UnitName.POUND)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(36)
                    )
                    .build(),
                Future.builder()
                    .name("Cheese Options")
                    .contractSize(20000)
                    .contractUnit(UnitName.POUND)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(37)
                    )
                    .build(),
                Future.builder()
                    .name("Pork Belly Options")
                    .contractSize(40000)
                    .contractUnit(UnitName.POUND)
                    .settlementDate(
                        OffsetDateTime.now()
                            .plusMonths(38)
                    )
                    .build()
            );

        futureRepository.saveAll(futures);
        LOGGER.info("future contracts seeded successfully");
    }

    private void exchangesSeeder() {
        List<Exchange> exchanges =
            List.of(
                Exchange.builder()
                    .exchangeName("Nasdaq")
                    .exchangeAcronym("NASDAQ")
                    .exchangeMICCode("XNAS")
                    .polity("USA")
                    .currency(CurrencyCode.USD)
                    .timeZone("America/New_York")
                    .openTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(9, 30),
                            ZoneOffset.of("-04:00")
                        )
                    )
                    .closeTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(16, 0),
                            ZoneOffset.of("-04:00")
                        )
                    )
                    .createdAt(LocalDate.now())
                    .build(),
                Exchange.builder()
                    .exchangeName("Jakarta Futures Exchange (bursa Berjangka Jakarta)")
                    .exchangeAcronym("BBJ")
                    .exchangeMICCode("XBBJ")
                    .polity("Indonesia")
                    .currency(CurrencyCode.EUR)
                    .timeZone("Asia/Jakarta")
                    .openTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(9, 0),
                            ZoneOffset.of("+07:00")
                        )
                    )
                    .closeTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(17, 30),
                            ZoneOffset.of("+07:00")
                        )
                    )
                    .createdAt(LocalDate.now())
                    .build(),
                Exchange.builder()
                    .exchangeName("Asx - Trade24")
                    .exchangeAcronym("SFE")
                    .exchangeMICCode("XSFE")
                    .polity("Australia")
                    .currency(CurrencyCode.AUD)
                    .timeZone("Australia/Melbourne")
                    .openTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(10, 0),
                            ZoneOffset.of("+10:00")
                        )
                    )
                    .closeTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(16, 0),
                            ZoneOffset.of("+10:00")
                        )
                    )
                    .createdAt(LocalDate.now())
                    .build(),
                Exchange.builder()
                    .exchangeName("Cboe Edga U.s. Equities Exchange Dark")
                    .exchangeAcronym("EDGADARK")
                    .exchangeMICCode("EDGD")
                    .polity("United States")
                    .currency(CurrencyCode.USD)
                    .timeZone("America/New_York")
                    .openTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(9, 30),
                            ZoneOffset.of("-04:00")
                        )
                    )
                    .closeTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(16, 0),
                            ZoneOffset.of("-04:00")
                        )
                    )
                    .createdAt(LocalDate.now())
                    .build(),
                Exchange.builder()
                    .exchangeName("Clear Street")
                    .exchangeAcronym("CLST")
                    .exchangeMICCode("CLST")
                    .polity("United States")
                    .currency(CurrencyCode.USD)
                    .timeZone("America/New_York")
                    .openTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(9, 30),
                            ZoneOffset.of("-04:00")
                        )
                    )
                    .closeTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(16, 0),
                            ZoneOffset.of("-04:00")
                        )
                    )
                    .createdAt(LocalDate.now())
                    .build(),
                Exchange.builder()
                    .exchangeName("Wall Street Access Nyc")
                    .exchangeAcronym("WABR")
                    .exchangeMICCode("WABR")
                    .polity("United States")
                    .currency(CurrencyCode.USD)
                    .timeZone("America/New_York")
                    .openTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(9, 30),
                            ZoneOffset.of("-04:00")
                        )
                    )
                    .closeTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(16, 0),
                            ZoneOffset.of("-04:00")
                        )
                    )
                    .createdAt(LocalDate.now())
                    .build(),
                Exchange.builder()
                    .exchangeName("Marex Spectron Europe Limited - Otf")
                    .exchangeAcronym("MSEL OTF")
                    .exchangeMICCode("MSEL")
                    .polity("Ireland")
                    .currency(CurrencyCode.EUR)
                    .timeZone("Europe/Dublin")
                    .openTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(8, 0),
                            ZoneOffset.of("+01:00")
                        )
                    )
                    .closeTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(16, 30),
                            ZoneOffset.of("+01:00")
                        )
                    )
                    .createdAt(LocalDate.now())
                    .build(),
                Exchange.builder()
                    .exchangeName("Borsa Italiana Equity Mtf")
                    .exchangeAcronym("BITEQMTF")
                    .exchangeMICCode("MTAH")
                    .polity("Italy")
                    .currency(CurrencyCode.EUR)
                    .timeZone("Europe/Rome")
                    .openTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(9, 0),
                            ZoneOffset.of("+02:00")
                        )
                    )
                    .closeTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(17, 25),
                            ZoneOffset.of("+02:00")
                        )
                    )
                    .createdAt(LocalDate.now())
                    .build(),
                Exchange.builder()
                    .exchangeName("Clearcorp Dealing Systems India Limited - Astroid")
                    .exchangeAcronym("ASTROID")
                    .exchangeMICCode("ASTR")
                    .polity("India")
                    .currency(CurrencyCode.USD)
                    .timeZone("Asia/Kolkata")
                    .openTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(9, 15),
                            ZoneOffset.of("+05:30")
                        )
                    )
                    .closeTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(15, 30),
                            ZoneOffset.of("+05:30")
                        )
                    )
                    .createdAt(LocalDate.now())
                    .build(),
                Exchange.builder()
                    .exchangeName("Memx Llc Equities")
                    .exchangeAcronym("MEMX")
                    .exchangeMICCode("MEMX")
                    .polity("United States")
                    .currency(CurrencyCode.USD)
                    .timeZone("America/New_York")
                    .openTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(9, 30),
                            ZoneOffset.of("-04:00")
                        )
                    )
                    .closeTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(16, 0),
                            ZoneOffset.of("-04:00")
                        )
                    )
                    .createdAt(LocalDate.now())
                    .build(),
                Exchange.builder()
                    .exchangeName("Natixis - Systematic Internaliser")
                    .exchangeAcronym("NATX")
                    .exchangeMICCode("NATX")
                    .polity("France")
                    .currency(CurrencyCode.EUR)
                    .timeZone("Europe/Paris")
                    .openTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(9, 0),
                            ZoneOffset.of("+02:00")
                        )
                    )
                    .closeTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(17, 30),
                            ZoneOffset.of("+02:00")
                        )
                    )
                    .createdAt(LocalDate.now())
                    .build(),
                Exchange.builder()
                    .exchangeName("Currenex Ireland Mtf - Rfq")
                    .exchangeAcronym("CNX MTF")
                    .exchangeMICCode("ICXR")
                    .polity("Ireland")
                    .currency(CurrencyCode.EUR)
                    .timeZone("Europe/Dublin")
                    .openTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(8, 0),
                            ZoneOffset.of("+01:00")
                        )
                    )
                    .closeTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(16, 30),
                            ZoneOffset.of("+01:00")
                        )
                    )
                    .createdAt(LocalDate.now())
                    .build(),
                Exchange.builder()
                    .exchangeName("Neo Exchange - Neo-l (market By Order)")
                    .exchangeAcronym("NEO-L")
                    .exchangeMICCode("NEOE")
                    .polity("Canada")
                    .currency(CurrencyCode.CAD)
                    .timeZone("America/Montreal")
                    .openTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(9, 30),
                            ZoneOffset.of("-04:00")
                        )
                    )
                    .closeTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(16, 0),
                            ZoneOffset.of("-04:00")
                        )
                    )
                    .createdAt(LocalDate.now())
                    .build(),
                Exchange.builder()
                    .exchangeName("Polish Trading Point")
                    .exchangeAcronym("PTP")
                    .exchangeMICCode("PTPG")
                    .polity("Poland")
                    .currency(CurrencyCode.EUR)
                    .timeZone("Europe/Warsaw")
                    .openTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(9, 0),
                            ZoneOffset.of("+02:00")
                        )
                    )
                    .closeTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(17, 35),
                            ZoneOffset.of("+02:00")
                        )
                    )
                    .createdAt(LocalDate.now())
                    .build(),
                Exchange.builder()
                    .exchangeName("Pfts Stock Exchange")
                    .exchangeAcronym("PFTS")
                    .exchangeMICCode("PFTS")
                    .polity("Ukraine")
                    .currency(CurrencyCode.EUR)
                    .timeZone("Europe/Kiev")
                    .openTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(10, 0),
                            ZoneOffset.of("+03:00")
                        )
                    )
                    .closeTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(17, 30),
                            ZoneOffset.of("+03:00")
                        )
                    )
                    .createdAt(LocalDate.now())
                    .build(),
                Exchange.builder()
                    .exchangeName("Cboe Australia - Transferable Custody Receipt Market")
                    .exchangeAcronym("CHI-X")
                    .exchangeMICCode("CXAR")
                    .polity("Australia")
                    .currency(CurrencyCode.AUD)
                    .timeZone("Australia/Melbourne")
                    .openTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(10, 0),
                            ZoneOffset.of("+10:00")
                        )
                    )
                    .closeTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(16, 0),
                            ZoneOffset.of("+10:00")
                        )
                    )
                    .createdAt(LocalDate.now())
                    .build(),
                Exchange.builder()
                    .exchangeName("Essex Radez Llc")
                    .exchangeAcronym("GLPS")
                    .exchangeMICCode("GLPS")
                    .polity("United States")
                    .currency(CurrencyCode.USD)
                    .timeZone("America/New_York")
                    .openTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(9, 30),
                            ZoneOffset.of("-04:00")
                        )
                    )
                    .closeTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(16, 0),
                            ZoneOffset.of("-04:00")
                        )
                    )
                    .createdAt(LocalDate.now())
                    .build(),
                Exchange.builder()
                    .exchangeName("London Metal Exchange")
                    .exchangeAcronym("LME")
                    .exchangeMICCode("XLME")
                    .polity("United Kingdom")
                    .currency(CurrencyCode.EUR)
                    .timeZone("Europe/London")
                    .openTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(8, 0),
                            ZoneOffset.of("+01:00")
                        )
                    )
                    .closeTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(16, 0),
                            ZoneOffset.of("+01:00")
                        )
                    )
                    .createdAt(LocalDate.now())
                    .build(),
                Exchange.builder()
                    .exchangeName("Multi Commodity Exchange Of India Ltd.")
                    .exchangeAcronym("MCX")
                    .exchangeMICCode("XIMC")
                    .polity("India")
                    .currency(CurrencyCode.JPY)
                    .timeZone("Asia/Kolkata")
                    .openTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(9, 15),
                            ZoneOffset.of("+05:30")
                        )
                    )
                    .closeTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(15, 30),
                            ZoneOffset.of("+05:30")
                        )
                    )
                    .createdAt(LocalDate.now())
                    .build(),
                Exchange.builder()
                    .exchangeName(
                        "Cassa Di Compensazione E Garanzia Spa - Ccp Agricultural Commodity Derivatives"
                    )
                    .exchangeAcronym("CCGAGRIDER")
                    .exchangeMICCode("CGGD")
                    .polity("Italy")
                    .currency(CurrencyCode.EUR)
                    .timeZone("Europe/Rome")
                    .openTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(9, 0),
                            ZoneOffset.of("+02:00")
                        )
                    )
                    .closeTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(17, 25),
                            ZoneOffset.of("+02:00")
                        )
                    )
                    .createdAt(LocalDate.now())
                    .build(),
                Exchange.builder()
                    .exchangeName("Toronto Stock Exchange - Drk")
                    .exchangeAcronym("TSX DRK")
                    .exchangeMICCode("XDRK")
                    .polity("Canada")
                    .currency(CurrencyCode.CAD)
                    .timeZone("America/Montreal")
                    .openTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(9, 30),
                            ZoneOffset.of("-04:00")
                        )
                    )
                    .closeTime(
                        OffsetDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(16, 0),
                            ZoneOffset.of("-04:00")
                        )
                    )
                    .createdAt(LocalDate.now())
                    .build()
            );

        exchangeRepository.saveAll(exchanges);
        LOGGER.info("exchanges seeded successfully");
    }
}
