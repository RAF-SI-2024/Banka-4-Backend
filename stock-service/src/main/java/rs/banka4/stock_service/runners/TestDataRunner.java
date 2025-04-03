package rs.banka4.stock_service.runners;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import rs.banka4.stock_service.domain.exchanges.db.Exchange;
import rs.banka4.stock_service.domain.listing.db.Listing;
import rs.banka4.stock_service.domain.listing.db.ListingDailyPriceInfo;
import rs.banka4.stock_service.domain.options.db.Option;
import rs.banka4.stock_service.domain.options.db.OptionsMaker;
import rs.banka4.stock_service.domain.security.Security;
import rs.banka4.stock_service.domain.security.forex.db.CurrencyCode;
import rs.banka4.stock_service.domain.security.forex.db.CurrencyMapper;
import rs.banka4.stock_service.domain.security.forex.db.ForexLiquidity;
import rs.banka4.stock_service.domain.security.forex.db.ForexPair;
import rs.banka4.stock_service.domain.security.future.db.Future;
import rs.banka4.stock_service.domain.security.future.db.UnitName;
import rs.banka4.stock_service.domain.security.stock.db.Stock;
import rs.banka4.stock_service.repositories.*;


@Component
@RequiredArgsConstructor
public class TestDataRunner implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestDataRunner.class);

    private final Environment environment;
    private final ForexRepository forexPairRepository;
    private final StockRepository stockRepository;
    private final FutureRepository futureRepository;
    private final ListingRepository listingRepository;
    private final ExchangeRepository exchangeRepository;
    private final ListingDailyPriceInfoRepository listingDailyPriceInfoRepository;
    private final OptionsRepository optionsRepository;
    private final OkHttpClient stockHttpClient;

    private Exchange srbForexExchange = null;
    private Exchange srbFutureExchange = null;

    @Value("${spring.alphavantage.api_key}")
    private String vantageKey = null;

    @Override
    public void run(String... args) {
        runProd();

        if (false && environment.matchesProfiles("dev")) {
            LOGGER.info("Inserting fake data (profiles includes 'dev')");
            seedDevStocks();
            seedDevForexPairs();
            seedDevFutures();
            seedDevExchanges();
        }
    }

    public void runProd() {
        LOGGER.info("Seeding prod");

        if (stockRepository.count() == 0) {
            seedProductionStocks();
        } else {
            LOGGER.info("Not reseeding stockRepository, data already exists");
        }

        if (forexPairRepository.count() == 0) {
            seedProductionForexPairs();
        } else {
            LOGGER.info("Not reseeding forexPairRepository, data already exists");
        }

        if (futureRepository.count() == 0) {
            seedProductionFutures();
        } else {
            LOGGER.info("Not reseeding futureRepository, data already exists");
        }

        if (exchangeRepository.count() == 0) {
            seedProductionExchanges();
        } else {
            LOGGER.info("Not reseeding exchangeRepository, data already exists");
        }

        if (listingRepository.count() == 0) {
            seedProductionListings();
        } else {
            LOGGER.info("Not reseeding listingsRepository, data already exists");
        }

        if (optionsRepository.count() == 0) {
            seedProductionOptions();
        } else {
            LOGGER.info("Not reseeding options, data already exists");
        }

        if (listingDailyPriceInfoRepository.count() == 0) {
            seedProductionListingDailyPriceInfo();
        } else {
            LOGGER.info("Not reseeding listing daily price info, data already exists");
        }

        LOGGER.info("Seeding prod finished, starting...");
    }

    private void seedProductionListingDailyPriceInfo() {
        try {
            List<ListingDailyPriceInfo> listingDailyPriceInfos = new ArrayList<>();
            BigDecimal change = BigDecimal.ZERO;

            for (Listing listing : listingRepository.findAll()) {
                change = change.add(BigDecimal.valueOf(0.25));
                if (change.compareTo(BigDecimal.valueOf(1.0)) > 0) {
                    change = BigDecimal.valueOf(-1.0);
                }

                BigDecimal baseBid = listing.getBid();
                BigDecimal baseAsk = listing.getAsk();
                BigDecimal bid = baseBid.subtract(change);
                BigDecimal ask = baseAsk.subtract(change);
                BigDecimal lastPrice =
                    ask.add(bid)
                        .divide(BigDecimal.valueOf(2), 6, RoundingMode.HALF_UP);

                ListingDailyPriceInfo info =
                    ListingDailyPriceInfo.builder()
                        .security(listing.getSecurity())
                        .exchange(listing.getExchange())
                        .date(
                            OffsetDateTime.of(
                                LocalDate.of(
                                    OffsetDateTime.now()
                                        .getYear(),
                                    4,
                                    1
                                ),
                                LocalTime.MIDNIGHT,
                                ZoneOffset.UTC
                            )
                        )
                        .lastPrice(lastPrice)
                        .askHigh(ask)
                        .bigLow(bid)
                        .change(change)
                        .volume(1000)
                        .build();

                listingDailyPriceInfos.add(info);
            }

            listingDailyPriceInfoRepository.saveAllAndFlush(listingDailyPriceInfos);
            LOGGER.info("Production ListingDailyPriceInfo seeded successfully.");
        } catch (Exception e) {
            LOGGER.error(
                "Error occurred while seeding ListingDailyPriceInfo: {}",
                e.getMessage(),
                e
            );
        }
    }

    private void seedProductionOptions() {
        try {
            OptionsMaker optionsMaker = new OptionsMaker();
            List<Option> options = new ArrayList<>();

            for (Listing listing : listingRepository.findAll()) {
                Security se = listing.getSecurity();
                if (!(se instanceof Stock)) {
                    continue;
                }
                Stock stock = (Stock) se;
                BigDecimal price =
                    listing.getAsk()
                        .add(listing.getBid())
                        .divide(BigDecimal.valueOf(2), 6, RoundingMode.HALF_UP);
                List<Option> lOptions = optionsMaker.generateOptions(stock, price);
                options.addAll(lOptions);
            }

            optionsRepository.saveAllAndFlush(options);
            LOGGER.info("Production options seeded successfully.");
        } catch (Exception e) {
            LOGGER.error("Error occurred while seeding prod options: {}", e.getMessage());
        }
    }

    private Listing fetchListingInfo(
        String ticker,
        long id,
        Stock stock,
        String exchangeAcronym,
        Map<String, Exchange> exchangesMap
    ) throws IOException {
        // https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=AAPL&apikey=7P4ANAS869M38S3B
        // koristi isti api za daily listings history

        StringBuilder sb = new StringBuilder();
        String url =
            sb.append("https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=")
                .append(ticker)
                .append("&apikey=")
                .append(vantageKey)
                .toString();

        Request request =
            new Request.Builder().url(url)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response =
            stockHttpClient.newCall(request)
                .execute();
        String jsonResponse =
            response.body()
                .string();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode listingJson =
            objectMapper.readTree(jsonResponse)
                .path("Global Quote");

        String priceStr =
            listingJson.path("05. price")
                .asText("N/A");
        String volumeStr =
            listingJson.path("06. volume")
                .asText("N/A");

        double price = Double.parseDouble(priceStr);
        long fakeContractSize = Long.parseLong(volumeStr) / 1000L;
        fakeContractSize = Long.max(1L, fakeContractSize);

        double fakeBid = price * 0.993;
        double fakeAsk = price * 1.007;
        /*
         * System.out.println("----------------"); System.out.println(exchangesMap.get(stock));
         * System.out.println(stock); System.out.println("--------------");
         */
        return Listing.builder()
            .id(new UUID(0L, id))
            .ask(new BigDecimal(fakeAsk))
            .bid(new BigDecimal(fakeBid))
            .contractSize((int) fakeContractSize)
            .active(true)
            .lastRefresh(OffsetDateTime.now())
            .exchange(exchangesMap.get(exchangeAcronym))
            .security(stock)
            .build();
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

    private void seedProductionListings() {
        Map<String, Exchange> exchangesMap = makeExchangeMap();
        Map<String, Stock> stocksMap = makeStockMap();
        try {
            List<Listing> listings = new ArrayList<>();
            List<String[]> records = new ArrayList<>();
            InputStream is =
                TestDataRunner.class.getClassLoader()
                    .getResourceAsStream("seeder_data/ticker_exchange.csv");
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

            long id = 11111111;
            for (String[] record : records) {
                String ticker = record[0];
                String exchange = record[1];
                // System.out.println(exchange); System.out.println(exchangesMap.keySet());
                Listing l =
                    fetchListingInfo(ticker, id++, stocksMap.get(ticker), exchange, exchangesMap);
                listings.add(l);
                Thread.sleep(200);
            }

            for (ForexPair fp : forexPairRepository.findAll()) {
                Listing.builder()
                    .id(new UUID(0L, id++))
                    .ask(fp.getExchangeRate())
                    .bid(fp.getExchangeRate())
                    .exchange(srbForexExchange)
                    .lastRefresh(OffsetDateTime.now())
                    .active(true)
                    .security(fp)
                    .contractSize(1)
                    .build();
            }

            for (Future f : futureRepository.findAll()) {
                Listing.builder()
                    .id(new UUID(0L, id++))
                    .ask(new BigDecimal(1000100))
                    .bid(new BigDecimal(1000000))
                    .exchange(srbFutureExchange)
                    .lastRefresh(OffsetDateTime.now())
                    .active(true)
                    .security(f)
                    .contractSize(1)
                    .build();
            }

            listingRepository.saveAllAndFlush(listings);
            LOGGER.info("Production listings seeded successfully.");
        } catch (Exception e) {
            LOGGER.error("Error occurred while seeding prod listings: {}", e.getMessage());
        }
    }

    private Stock fetchStockInfo(String ticker, long id) throws IOException {
        // https://www.alphavantage.co/query?function=OVERVIEW&symbol=IBM&apikey=demo
        String url =
            "https://www.alphavantage.co/query?function=OVERVIEW&symbol="
                + ticker
                + "&apikey="
                + vantageKey;
        Request request =
            new Request.Builder().url(url)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response =
            stockHttpClient.newCall(request)
                .execute();
        String jsonResponse =
            response.body()
                .string();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode stockJson = objectMapper.readTree(jsonResponse);

        String name =
            stockJson.path("Name")
                .asText("N/A");
        String dividendYield =
            stockJson.path("DividendYield")
                .asText("N/A");
        String outstandingShares =
            stockJson.path("SharesOutstanding")
                .asText("N/A");
        String marketCap =
            stockJson.path("MarketCapitalization")
                .asText("N/A");

        /*
         * System.out.println("divident yield: " + dividendYield);
         * System.out.println(outstandingShares); System.out.println(marketCap);
         * System.out.println(name); System.out.println("--------------");
         */

        BigDecimal divYield = new BigDecimal(0.0);
        try {
            divYield = new BigDecimal(dividendYield);
        } catch (Exception e) {
        }

        long outstandingSharesLong;
        try {
            outstandingSharesLong = Long.parseLong(outstandingShares);
        } catch (Exception e) {
            return null;
        }

        return Stock.builder()
            .id(new UUID(0L, id))
            .name(name)
            .dividendYield(divYield)
            .outstandingShares(outstandingSharesLong)
            .createdAt(OffsetDateTime.now())
            .marketCap(new BigDecimal(marketCap))
            .ticker(ticker)
            .build();
    }

    private void seedProductionStocks() {
        try {
            List<Stock> stocks = new ArrayList<>();
            List<String> tickers = new ArrayList<>();
            InputStream is =
                TestDataRunner.class.getClassLoader()
                    .getResourceAsStream("seeder_data/ticker_exchange.csv");
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
                String t = line.split(",")[0].strip();
                tickers.add(t);
            }

            long id = 545353;
            for (String ticker : tickers) {
                Stock stock = fetchStockInfo(ticker, id++);
                if (stock == null) {
                    continue;
                }
                stocks.add(stock);
                Thread.sleep(200);
            }
            stockRepository.saveAllAndFlush(stocks);
            LOGGER.info("Production stocks seeded successfully.");
        } catch (Exception e) {
            LOGGER.error("Error occurred while seeding prod stocks: {}", e.getMessage());
        }
    }

    private void seedDevStocks() {
    }

    private JsonNode fetchExchangeRateData() throws IOException, InterruptedException {
        Thread.sleep(10 * 1000);
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request =
            HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/exchange/exchange-rate"))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(response.body());
    }

    private double getCurrencyValue(String currencyCode, JsonNode exchangeData) {
        if (currencyCode.equals("RSD")) {
            return 1.0;
        }
        JsonNode rate =
            exchangeData.path("exchanges")
                .path(currencyCode);
        return rate.path("Neutral")
            .asDouble();
    }


    private void seedForexPairs() {
        JsonNode exchangeData = null;
        try {
            exchangeData = fetchExchangeRateData();
        } catch (IOException | InterruptedException e) {
            LOGGER.error(
                "Fetch error. Forex prod seeder failed to fetch exchange office. {}",
                e.getMessage()
            );
        }

        ForexLiquidity[] forexLiquidities = ForexLiquidity.values();
        int i = 0;
        long id = 52055;
        long ms_id = 0;
        List<ForexPair> forexPairs = new ArrayList<>();
        for (CurrencyCode currencyCode1 : CurrencyCode.values()) {
            for (CurrencyCode currencyCode2 : CurrencyCode.values()) {
                if (currencyCode1.equals(currencyCode2)) {
                    continue;
                }

                String ticker = currencyCode1.name() + "/" + currencyCode2.name();
                double val = -1.0;
                try {
                    double v1 =
                        getCurrencyValue(
                            currencyCode1.name()
                                .toUpperCase(),
                            exchangeData
                        );
                    double v2 =
                        getCurrencyValue(
                            currencyCode2.name()
                                .toUpperCase(),
                            exchangeData
                        );
                    val = v1 / v2;
                } catch (Exception e) {
                    LOGGER.error("Json exception in calculating forex pair value");
                }

                forexPairs.add(
                    ForexPair.builder()
                        .id(new UUID(ms_id, id++))
                        .baseCurrency(currencyCode1)
                        .quoteCurrency(currencyCode2)
                        .liquidity(forexLiquidities[i++])
                        .exchangeRate(new BigDecimal(val))
                        .ticker(ticker.toUpperCase())
                        .name(ticker)
                        .build()
                );
                i = i % 3;
            }
        }
        forexPairRepository.saveAllAndFlush(forexPairs);
        LOGGER.info("Production forex pairs seeded successfully");
    }


    private void seedProductionForexPairs() {
        List<String> forexPairs = new ArrayList<>();
        for (int i = 0; i < CurrencyCode.values().length; i++) {
            for (int j = 0; j < CurrencyCode.values().length; j++) {
                if (i == j) continue;

                // FETCHING THE FOREX PAIRS
//                String url =
//                    "https://www.alphavantage.co/query?function=CURRENCY_EXCHANGE_RATE&from_currency="
//                        + CurrencyCode.values()[i]
//                        + "&to_currency="
//                        + CurrencyCode.values()[j]
//                        + "&apikey="
//                        + vantageKey;

                HttpUrl.Builder urlBuilder =
                    HttpUrl.parse("https://test.com/query?")
                        .newBuilder()
                        .addQueryParameter("function", "CURRENCY_EXCHANGE_RATE")
                        .addQueryParameter("from_currency", CurrencyCode.values()[i].name())
                        .addQueryParameter("to_currency", CurrencyCode.values()[j].name())
                        .addQueryParameter("apikey", vantageKey);

                String url =
                    urlBuilder.build()
                        .toString();

                Request request =
                    new Request.Builder().url(url)
                        .addHeader("Content-Type", "application/json")
                        .build();
                try {
                    Response response =
                        stockHttpClient.newCall(request)
                            .execute();
                    String jsonResponse =
                        response.body()
                            .string();
                    forexPairs.add(jsonResponse);
                    Thread.sleep(200);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage());
                }
            }
        }

        for (String s : forexPairs) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode root = objectMapper.readTree(s);
                JsonNode stockJson = root.path("Realtime Currency Exchange Rate");

                String fromCode =
                    stockJson.path("1. From_Currency Code")
                        .asText("N/A");
                String toCode =
                    stockJson.path("3. To_Currency Code")
                        .asText("N/A");
                String ticker = fromCode + "/" + toCode;
                String exchangeRate =
                    stockJson.path("5. Exchange Rate")
                        .asText("N/A");

                if (fromCode.equals("N/A")) continue;

                ForexPair forexPair =
                    ForexPair.builder()
                        .baseCurrency(CurrencyCode.valueOf(fromCode.toUpperCase()))
                        .quoteCurrency(CurrencyCode.valueOf(toCode.toUpperCase()))
                        .exchangeRate(new BigDecimal(exchangeRate))
                        .liquidity(ForexLiquidity.LOW)
                        .ticker(ticker.toUpperCase())
                        .name(ticker)
                        .build();

                forexPairRepository.saveAndFlush(forexPair);
            } catch (Exception e) {
                LOGGER.error("Forex pair seeder failed {}", e.getMessage());
            }
        }
        LOGGER.info("forex pairs seeded successfully");
    }

    private void seedDevForexPairs() {
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
                        .exchangeRate(new BigDecimal("3.5"))
                        .build()
                );
                i = i % 3;
            }
        }
        forexPairRepository.saveAllAndFlush(forexPairs);
        LOGGER.info("forex pairs seeded successfully");
    }

    private String makeFakeTickerForFuture(String name, OffsetDateTime settlementDate) {
        String[] letters = {
            "F","G","H","J","K","M","N","Q","U","V","X","Z"
        };
        String base = getBaseTicker(name);

        StringBuilder sb = new StringBuilder();
        String monthCode = letters[settlementDate.getMonthValue() - 1];
        int year = settlementDate.getYear() % 100;
        sb.append(base);
        sb.append(monthCode);
        sb.append(year);
        return sb.toString()
            .toUpperCase();
    }

    private static String getBaseTicker(String name) {
        String[] words = name.split("[-_ ]");

        String base;
        if (words.length > 1) {
            StringBuilder sb = new StringBuilder();
            for (String word : words) {
                if (!word.isEmpty()) {
                    sb.append(word.charAt(0));
                }
            }
            base = sb.toString();
        } else {
            if (name.length() >= 3) {
                base = name.substring(0, 3);
            } else {
                base = name;
            }
        }
        return base;
    }

    private void seedProductionFutures() {
        try {
            List<Future> futures = new ArrayList<>();
            List<String[]> records = new ArrayList<>();
            InputStream is =
                TestDataRunner.class.getClassLoader()
                    .getResourceAsStream("seeder_data/future_data.csv");
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

                String[] fields = line.split(",");
                records.add(fields);
            }

            int i = 1;
            long id = 5215;
            long ms_id = 0;
            for (String[] record : records) {
                String name = record[0];
                long size = Long.parseLong(record[1]);
                UnitName unitName =
                    UnitName.valueOf(
                        record[2].toUpperCase()
                            .replace(" ", "_")
                    );
                long margin = Long.parseLong(record[3]);
                String type = record[4];

                OffsetDateTime settlementDate =
                    OffsetDateTime.now()
                        .plusMonths(i);
                i = (i % 28) + 1;

                String ticker = makeFakeTickerForFuture(name, settlementDate);
                futures.add(
                    Future.builder()
                        .id(new UUID(ms_id, id++))
                        .name(name)
                        .contractSize(size)
                        .contractUnit(unitName)
                        .settlementDate(settlementDate)
                        .ticker(ticker)
                        .build()
                );
            }
            futureRepository.saveAllAndFlush(futures);
            LOGGER.info("future contracts seeded successfully");
        } catch (Exception e) {
            LOGGER.error("Error occurred while reading CSV file: {}", e.getMessage());
        }

    }

    private void seedDevFutures() {
    }

    private void seedProductionExchanges() {
        try {
            List<Exchange> exchanges = new ArrayList<>();
            InputStream is =
                TestDataRunner.class.getClassLoader()
                    .getResourceAsStream("seeder_data/exchanges.csv");
            if (is == null) {
                throw new IllegalArgumentException("Exchange CSV file not found!");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            boolean isHeader = true;
            long id = 7000;
            long ms_id = 0;

            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] fields = line.split(",");

                String name = fields[0];
                String acronym = fields[1];
                String micCode = fields[2];
                String country = fields[3];
                String currency = fields[4];
                String timeZone = fields[5];
                LocalTime openTime = LocalTime.parse(fields[6].strip());
                LocalTime closeTime = LocalTime.parse(fields[7].strip());

                LocalDate fixedDate = LocalDate.of(2000, 1, 1);
                LocalDateTime openDateTimeLocal = LocalDateTime.of(fixedDate, openTime);
                LocalDateTime closeDateTimeLocal = LocalDateTime.of(fixedDate, closeTime);

                ZoneId zoneId = ZoneId.of(timeZone);
                ZoneOffset openOffset =
                    zoneId.getRules()
                        .getOffset(openDateTimeLocal);
                ZoneOffset closeOffset =
                    zoneId.getRules()
                        .getOffset(closeDateTimeLocal);

                OffsetDateTime openDateTime = OffsetDateTime.of(openDateTimeLocal, openOffset);
                OffsetDateTime closeDateTime = OffsetDateTime.of(closeDateTimeLocal, closeOffset);

                CurrencyCode cc = null;
                try {
                    cc = CurrencyMapper.mapToCurrencyCode(currency);
                    if (cc == null) {
                        throw new IllegalArgumentException();
                    }
                } catch (IllegalArgumentException e) {
                    continue;
                }

                Exchange exchange =
                    Exchange.builder()
                        .id(new UUID(ms_id, id++))
                        .timeZone(timeZone)
                        .exchangeMICCode(micCode)
                        .currency(cc)
                        .polity(country)
                        .exchangeAcronym(acronym)
                        .exchangeName(name)
                        .openTime(openDateTime)
                        .closeTime(closeDateTime)
                        .createdAt(LocalDate.now())
                        .build();

                exchanges.add(exchange);
            }

            String timeZone = "Europe/Belgrade";

            LocalTime openTime = LocalTime.of(0, 0);
            LocalTime closeTime = LocalTime.of(23, 59);

            LocalDate fixedDate = LocalDate.of(2000, 1, 1);
            LocalDateTime openDateTimeLocal = LocalDateTime.of(fixedDate, openTime);
            LocalDateTime closeDateTimeLocal = LocalDateTime.of(fixedDate, closeTime);

            ZoneId zoneId = ZoneId.of(timeZone);
            ZoneOffset openOffset =
                zoneId.getRules()
                    .getOffset(openDateTimeLocal);
            ZoneOffset closeOffset =
                zoneId.getRules()
                    .getOffset(closeDateTimeLocal);

            OffsetDateTime openDateTime = OffsetDateTime.of(openDateTimeLocal, openOffset);
            OffsetDateTime closeDateTime = OffsetDateTime.of(closeDateTimeLocal, closeOffset);

            Exchange exchange =
                Exchange.builder()
                    .id(new UUID(ms_id, id++))
                    .timeZone(timeZone)
                    .exchangeMICCode("FORX")
                    .currency(CurrencyCode.RSD)
                    .polity("Serbia")
                    .exchangeAcronym("SRBFORX")
                    .exchangeName("Serbia Forex")
                    .openTime(openDateTime)
                    .closeTime(closeDateTime)
                    .createdAt(LocalDate.now())
                    .build();

            srbForexExchange = exchange;
            exchanges.add(exchange);

            exchange =
                Exchange.builder()
                    .id(new UUID(ms_id, id++))
                    .timeZone(timeZone)
                    .exchangeMICCode("FUTU")
                    .currency(CurrencyCode.RSD)
                    .polity("Serbia")
                    .exchangeAcronym("SRBFUTU")
                    .exchangeName("Serbia Futures")
                    .openTime(openDateTime)
                    .closeTime(closeDateTime)
                    .createdAt(LocalDate.now())
                    .build();

            srbFutureExchange = exchange;
            exchanges.add(exchange);

            exchangeRepository.saveAllAndFlush(exchanges);
            LOGGER.info("Exchange data seeded successfully");
        } catch (Exception e) {
            LOGGER.error("Error occurred while reading Exchange CSV: {}", e.getMessage());
        }
    }

    private void seedDevExchanges() {
    }
}
