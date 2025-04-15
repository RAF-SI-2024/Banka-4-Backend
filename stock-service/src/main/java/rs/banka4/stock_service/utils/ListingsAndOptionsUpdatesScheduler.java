package rs.banka4.stock_service.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rs.banka4.rafeisen.common.currency.CurrencyCode;
import rs.banka4.stock_service.config.retrofit.AlphaVantageService;
import rs.banka4.stock_service.domain.exchanges.db.Exchange;
import rs.banka4.stock_service.domain.listing.db.Listing;
import rs.banka4.stock_service.domain.listing.dtos.ListingApiDto;
import rs.banka4.stock_service.domain.options.db.Option;
import rs.banka4.stock_service.domain.options.db.OptionsMaker;
import rs.banka4.stock_service.domain.security.Security;
import rs.banka4.stock_service.domain.security.forex.db.ForexLiquidity;
import rs.banka4.stock_service.domain.security.forex.db.ForexPair;
import rs.banka4.stock_service.domain.security.forex.dtos.ForexPairApiDto;
import rs.banka4.stock_service.domain.security.future.db.Future;
import rs.banka4.stock_service.domain.security.stock.db.Stock;
import rs.banka4.stock_service.exceptions.ExchangeAcronymNotFound;
import rs.banka4.stock_service.repositories.*;
import rs.banka4.stock_service.runners.TestDataRunner;


@Profile("!test")
@Component
@RequiredArgsConstructor
public class ListingsAndOptionsUpdatesScheduler {
    // For now ignoring ForexPairs updates, they their prices in their own model for some reason
    // TODO first update ^
    private static final Logger LOGGER =
        LoggerFactory.getLogger(ListingsAndOptionsUpdatesScheduler.class);

    private final ForexRepository forexPairRepository;
    private final StockRepository stockRepository;
    private final FutureRepository futureRepository;
    private final ListingRepository listingRepository;
    private final ExchangeRepository exchangeRepository;
    private final ListingDailyPriceInfoRepository listingDailyPriceInfoRepository;
    private final OptionsRepository optionsRepository;
    private final AlphaVantageService alphaRetrofit;

    private Map<String, Exchange> exchangesMap;
    private Map<String, Stock> stocksMap;

    private Exchange srbForexExchange = null;
    private Exchange srbFutureExchange = null;

    @Value("${alphavantage.api-key}")
    private String vantageKey;


    public Exchange getExchange(String acronym) {
        return exchangeRepository.findByExchangeAcronym(acronym)
            .orElseThrow(
                () -> new ExchangeAcronymNotFound("Exchange with acronym " + acronym + " not found")
            );
    }

    @Scheduled(fixedDelayString = "#{${listings.refetch-min} * 60l * 1000l}")
    @Transactional
    public void updateListingsAndOptions() {
        if (!TestDataRunner.finishedSeeding) {
            System.out.println(
                "Database not seeded yet. Skipping scheduled update of listings and options."
            );
            return;
        }

        LOGGER.info(
            "{} starting scheduled update of listings and options",
            OffsetDateTime.now()
                .toLocalTime()
                .truncatedTo(ChronoUnit.SECONDS)
        );

        try {
            refreshForexPairs();
            LOGGER.info("Forex pairs updated");
        } catch (Exception e) {
            LOGGER.error("Error occurred while fetching forex pairs: {}", e.getMessage());
        }

        long listingsBefore = listingRepository.count();
        long optionsBefore = optionsRepository.count();

        // throwing an error after logging in so the @Transaction would fail
        try {
            updateListings();
            LOGGER.info("Listings updated");
        } catch (Exception e) {
            LOGGER.error("Error occurred while fetching listings: {}", e.getMessage());
            throw new RuntimeException("Error occurred while fetching listings", e);
        }

        try {
            updateOptions();
            LOGGER.info("Options updated");
        } catch (Exception e) {
            LOGGER.error("Error occurred while fetching listings: {}", e.getMessage());
            throw new RuntimeException("Error occurred while making options", e);
        }

        long listingsAfter = listingRepository.count();
        long optionsAfter = optionsRepository.count();

        LOGGER.info("Listings: before = {}, after = {}", listingsBefore, listingsAfter);
        LOGGER.info("Options:  before = {}, after = {}", optionsBefore, optionsAfter);
    }

    Map<String, Exchange> makeExchangeMap() {
        Map<String, Exchange> exchangesMap = new HashMap<>();
        for (Exchange exchange : exchangeRepository.findAll()) {
            exchangesMap.put(exchange.getExchangeAcronym(), exchange);
        }
        return exchangesMap;
    }

    Map<String, Stock> makeStockMap() {
        Map<String, Stock> stocksMap = new HashMap<>();
        for (Stock stock : stockRepository.findAll()) {
            stocksMap.put(stock.getTicker(), stock);
        }
        return stocksMap;
    }

    private Listing fetchListingInfo(
        String ticker,
        Stock stock,
        String exchangeAcronym,
        Map<String, Exchange> exchangesMap
    ) throws IOException {
        retrofit2.Call<ListingApiDto> call =
            alphaRetrofit.getListingInfo("GLOBAL_QUOTE", ticker, vantageKey);

        ListingApiDto listingApiDto =
            call.execute()
                .body();

        double price = 0.0;
        long fakeContractSize = 0L;
        try {
            price =
                Double.parseDouble(
                    listingApiDto.globalQuoteDto()
                        .priceStr()
                );
            fakeContractSize =
                Long.parseLong(
                    listingApiDto.globalQuoteDto()
                        .volumeStr()
                ) / 1000L;
            fakeContractSize = Long.max(1L, fakeContractSize);
        } catch (Exception e) {
            LOGGER.error("{}", e.getMessage());
        }

        double fakeBid = price * 0.993;
        double fakeAsk = price * 1.007;
        /*
         * System.out.println("----------------"); System.out.println(exchangesMap.get(stock));
         * System.out.println(stock); System.out.println("--------------");
         */

        Exchange exchange = exchangesMap.get(exchangeAcronym);
        OffsetDateTime now =
            ZonedDateTime.now(ZoneId.of("Europe/Belgrade"))
                .toOffsetDateTime();

        return Listing.builder()
            .id(UUID.randomUUID())
            .ask(new BigDecimal(fakeAsk))
            .bid(new BigDecimal(fakeBid))
            .contractSize((int) fakeContractSize)
            .active(true)
            .lastRefresh(now.minusDays(1))
            .exchange(exchange)
            .security(stock)
            .build();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateListings() throws IOException, InterruptedException {
        try {
            srbForexExchange = getExchange("SRBFORX");
            srbFutureExchange = getExchange("SRBFUTU");
        } catch (ExchangeAcronymNotFound e) {
            LOGGER.error("Failed to select Serbian Forex / Future exchange. " + e.getMessage());
            LOGGER.error("Consider reseeding the database. Crashing...");
            System.exit(1);
        }

        exchangesMap = makeExchangeMap();
        stocksMap = makeStockMap();

        List<Listing> listings = new ArrayList<>();
        List<String[]> records = new ArrayList<>();
        InputStream is = TestDataRunner.class.getResourceAsStream("ticker_exchange.csv");
        if (is == null) {
            throw new IllegalArgumentException("File not found!");
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        boolean isHeader = true;

        while ((line = reader.readLine()) != null) {
            if (isHeader) {
                isHeader = false;
                continue;
            }
            records.add(line.split(","));
        }

        for (String[] record : records) {
            String ticker = record[0];
            String exchange = record[1];
            if (stocksMap.get(ticker) == null) continue;
            Listing l = fetchListingInfo(ticker, stocksMap.get(ticker), exchange, exchangesMap);
            listings.add(l);
            Thread.sleep(200);
        }

        for (ForexPair fp : forexPairRepository.findAll()) {
            listings.add(
                Listing.builder()
                    .id(UUID.randomUUID())
                    .ask(fp.getExchangeRate())
                    .bid(fp.getExchangeRate())
                    .exchange(srbForexExchange)
                    .lastRefresh(OffsetDateTime.now())
                    .active(true)
                    .security(fp)
                    .contractSize(1)
                    .build()
            );
        }

        for (Future f : futureRepository.findAll()) {
            listings.add(
                Listing.builder()
                    .id(UUID.randomUUID())
                    .ask(new BigDecimal(1000100))
                    .bid(new BigDecimal(1000000))
                    .exchange(srbFutureExchange)
                    .lastRefresh(OffsetDateTime.now())
                    .active(true)
                    .security(f)
                    .contractSize(1)
                    .build()
            );
        }

        listingRepository.deactivateAll();
        listingRepository.saveAllAndFlush(listings);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateOptions() {
        OptionsMaker optionsMaker = new OptionsMaker();
        List<Option> options = new ArrayList<>();

        for (Listing listing : listingRepository.findAllActiveListings()) {
            Security se = listing.getSecurity();
            if (!(se instanceof Stock)) {
                continue;
            }
            Stock stock = (Stock) se;
            BigDecimal price =
                listing.getAsk()
                    .add(listing.getBid())
                    .divide(BigDecimal.valueOf(2), 6, RoundingMode.HALF_UP);
            List<Option> lOptions =
                optionsMaker.generateOptions(
                    stock,
                    price,
                    listing.getExchange()
                        .getCurrency()
                );
            options.addAll(lOptions);
        }

        optionsRepository.deleteOptionsWithoutOrders();
        optionsRepository.deactivateAll();
        optionsRepository.saveAllAndFlush(options);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void refreshForexPairs() throws IOException, InterruptedException {
        List<ForexPair> forexPairs = new ArrayList<>();

        for (int i = 0; i < CurrencyCode.values().length; i++) {
            for (int j = 0; j < CurrencyCode.values().length; j++) {
                if (i == j) continue;

                // FETCHING THE FOREX PAIRS
                retrofit2.Call<ForexPairApiDto> call =
                    alphaRetrofit.getForexPair(
                        "CURRENCY_EXCHANGE_RATE",
                        CurrencyCode.values()[i].name(),
                        CurrencyCode.values()[j].name(),
                        vantageKey
                    );


                retrofit2.Response<ForexPairApiDto> response2 = call.execute();
                ForexPairApiDto forexPairApiDto = response2.body();
                Thread.sleep(200);

                if (forexPairApiDto.realTimeCurrencyExchangeRate() == null) continue;

                CurrencyCode baseCurrency =
                    forexPairApiDto.realTimeCurrencyExchangeRate()
                        .baseCurrency();
                CurrencyCode quoteCurrency =
                    forexPairApiDto.realTimeCurrencyExchangeRate()
                        .quoteCurrency();

                String ticker =
                    baseCurrency.name()
                        .toUpperCase()
                        + "/"
                        + quoteCurrency.name()
                            .toUpperCase();
                String name =
                    baseCurrency.name()
                        .toUpperCase()
                        + " to "
                        + quoteCurrency.name()
                            .toUpperCase();

                UUID id;

                Optional<ForexPair> existing = forexPairRepository.findByTicker(ticker);
                if (existing.isPresent()) {
                    id =
                        existing.get()
                            .getId();
                } else {
                    id = UUID.randomUUID();
                }

                ForexPair forexPair =
                    ForexPair.builder()
                        .id(id)
                        .baseCurrency(baseCurrency)
                        .quoteCurrency(quoteCurrency)
                        .liquidity(ForexLiquidity.LOW)
                        .exchangeRate(
                            forexPairApiDto.realTimeCurrencyExchangeRate()
                                .exchangeRate()
                        )
                        .ticker(ticker)
                        .name(name)
                        .build();

                forexPairs.add(forexPair);
            }
        }

        forexPairRepository.saveAllAndFlush(forexPairs);
        // This will overwrite the old id's if said forex pairs exist in the db This whole refresh
        // is happening only because we save exchange rate (price) in both ForexPair and its Listing
    }
}
