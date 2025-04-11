package rs.banka4.stock_service.integration;


import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import rs.banka4.stock_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.stock_service.domain.exchanges.db.Exchange;
import rs.banka4.stock_service.domain.listing.db.Listing;
import rs.banka4.stock_service.domain.orders.db.Direction;
import rs.banka4.stock_service.domain.orders.db.Order;
import rs.banka4.stock_service.domain.orders.db.OrderType;
import rs.banka4.stock_service.domain.orders.db.Status;
import rs.banka4.stock_service.domain.security.Security;
import rs.banka4.stock_service.domain.security.forex.db.CurrencyCode;
import rs.banka4.stock_service.domain.security.stock.db.Stock;
import rs.banka4.stock_service.repositories.*;
import rs.banka4.stock_service.utils.AssetGenerator;
import rs.banka4.stock_service.utils.ExchangeGenerator;
import rs.banka4.stock_service.utils.ListingGenerator;
import rs.banka4.testlib.integration.DbEnabledTest;
import rs.banka4.testlib.utils.JwtPlaceholders;

@SpringBootTest
@DbEnabledTest
@AutoConfigureMockMvc
public class SecuritiesControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private SecurityRepository securityRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ListingRepository listingRepo;

    @Autowired
    private ExchangeRepository exchangeRepo;

    @Autowired
    private ListingDailyPriceInfoRepository listingHistoryRepo;

    private UUID userId;

    @BeforeEach
    void setup() {
        userId = UUID.fromString(JwtPlaceholders.V3_VALID_CLIENT_TOKEN);

        // Clear previous test data
        orderRepository.deleteAll();
        listingRepo.deleteAll();
        exchangeRepo.deleteAll();
        assetRepository.deleteAll();

        // Setup exchange
        Exchange ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);

        // Save assets with UUID tickers
        AssetGenerator.makeExampleAssets()
            .forEach(asset -> {
                asset.setTicker(
                    asset.getId()
                        .toString()
                ); // Fix ticker format
                assetRepository.saveAndFlush(asset);
            });

        // Create listings for all securities
        securityRepository.findAll()
            .forEach(security -> {
                ListingGenerator.makeExampleListings(
                    security,
                    ber1,
                    listingRepo,
                    listingHistoryRepo
                );
            });
    }

    @Test
    public void testGetMySecurities_basicHoldings() throws Exception {
        // Create test orders
        Security stock =
            securityRepository.findById(AssetGenerator.STOCK_EX1_UUID)
                .orElseThrow();

        Security option =
            securityRepository.findById(AssetGenerator.OPTION_EX1_CALL_UUID)
                .orElseThrow();

        // Stock transactions
        createOrder(stock, 30, "150.00", Direction.BUY, 1);
        createOrder(stock, 10, "160.00", Direction.SELL, 2);

        // Option transaction
        createOrder(option, 2, "5.00", Direction.BUY, 3);

        // Update listing prices
        updateListingPrice(stock, "172.50");
        updateListingPrice(option, "7.50");

        mvc.get()
            .uri("/securities/me")
            .header("Authorization", "Bearer " + JwtPlaceholders.CLIENT_TOKEN)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                [
                  {
                    "type": "Option",
                    "ticker": "b5b843a4-c90c-4a14-8bdf-06da1b543f97",
                    "amount": 2,
                    "price": 7.50,
                    "profit": 5.00,
                    "lastModified": "@matchesDatePattern('yyyy-MM-ddTHH:mm:ssXXX')@"
                  },
                  {
                    "type": "Stock",
                    "ticker": "c6a3ad44-0eee-4bd3-addf-d8f2086b6f51",
                    "amount": 20,
                    "price": 172.50,
                    "profit": 450.00,
                    "lastModified": "@matchesDatePattern('yyyy-MM-ddTHH:mm:ssXXX')@"
                  }
                ]
                """);
    }

    @Test
    public void testGetTotalStockProfit_mixedSecurities() throws Exception {
        // Get required securities
        Stock stock1 =
            (Stock) securityRepository.findById(AssetGenerator.STOCK_EX1_UUID)
                .orElseThrow();
        Stock stock2 =
            (Stock) securityRepository.findById(AssetGenerator.STOCK_EX2_UUID)
                .orElseThrow();
        Security option =
            securityRepository.findById(AssetGenerator.OPTION_EX1_CALL_UUID)
                .orElseThrow();
        Security forex =
            securityRepository.findById(AssetGenerator.FOREX_EUR_USD_UUID)
                .orElseThrow();

        // Create listings with current prices
        createListingWithPrice(stock1, "200.00");
        createListingWithPrice(stock2, "1500.00");
        createListingWithPrice(option, "25.00");
        createListingWithPrice(forex, "1.15");

        // Create orders
        createBuyOrder(stock1, 10, "180.00"); // Profit: (200-180)*10 = 200
        createBuyOrder(stock2, 2, "1400.00"); // Profit: (1500-1400)*2 = 200
        createBuyOrder(option, 5, "20.00"); // Should be ignored
        createBuyOrder(forex, 1000, "1.10"); // Should be ignored

        mvc.get()
            .uri("/securities/me/total-profit")
            .header("Authorization", "Bearer " + JwtPlaceholders.CLIENT_TOKEN)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                {
                  "totalProfit": 400.00,
                  "currency": "USD"
                }
                """);
    }

    @Test
    public void testGetTotalStockProfit_noStocks() throws Exception {
        // Create non-stock holdings
        Security option =
            securityRepository.findById(AssetGenerator.OPTION_EX1_CALL_UUID)
                .orElseThrow();
        Security forex =
            securityRepository.findById(AssetGenerator.FOREX_EUR_USD_UUID)
                .orElseThrow();

        createListingWithPrice(option, "25.00");
        createListingWithPrice(forex, "1.15");
        createBuyOrder(option, 5, "20.00");
        createBuyOrder(forex, 1000, "1.10");

        mvc.get()
            .uri("/securities/me/total-profit")
            .header("Authorization", "Bearer " + JwtPlaceholders.CLIENT_TOKEN)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                {
                  "totalProfit": 0.00,
                  "currency": "USD"
                }
                """);
    }

    @Test
    public void testGetTotalStockProfit_negativeProfit() throws Exception {
        Stock stock =
            (Stock) securityRepository.findById(AssetGenerator.STOCK_EX1_UUID)
                .orElseThrow();

        createListingWithPrice(stock, "150.00");
        createBuyOrder(stock, 100, "160.00"); // Profit: (150-160)*100 = -1000

        mvc.get()
            .uri("/securities/me/total-profit")
            .header("Authorization", "Bearer " + JwtPlaceholders.CLIENT_TOKEN)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                {
                  "totalProfit": -1000.00,
                  "currency": "USD"
                }
                """);
    }

    private void createBuyOrder(Security security, int quantity, String buyPrice) {
        Order order =
            Order.builder()
                .userId(this.userId)
                .asset(security)
                .quantity(quantity)
                .pricePerUnit(new MonetaryAmount(new BigDecimal(buyPrice), CurrencyCode.USD))
                .direction(Direction.BUY)
                .isDone(true)
                .build();
        orderRepository.save(order);
    }

    private void createListingWithPrice(Security security, String price) {
        Exchange exchange =
            exchangeRepo.findAll()
                .get(0);
        Listing listing =
            Listing.builder()
                .security(security)
                .exchange(exchange)
                .ask(new BigDecimal(price))
                .build();
        listingRepo.save(listing);
    }

    private void createOrder(
        Security security,
        int quantity,
        String price,
        Direction direction,
        int hoursAgo
    ) {
        orderRepository.save(
            Order.builder()
                .userId(userId)
                .asset(security)
                .orderType(OrderType.LIMIT)
                .quantity(quantity)
                .contractSize(1)
                .pricePerUnit(new MonetaryAmount(new BigDecimal(price), CurrencyCode.USD))
                .direction(direction)
                .status(Status.APPROVED)
                .isDone(true)
                .lastModified(
                    OffsetDateTime.now()
                        .minusHours(hoursAgo)
                )
                .createdAt(
                    OffsetDateTime.now()
                        .minusHours(hoursAgo)
                )
                .build()
        );
    }

    private void updateListingPrice(Security security, String price) {
        Listing listing = listingRepo.findListingBySecurity(security);
        listing.setAsk(new BigDecimal(price));
        listingRepo.save(listing);
    }
}
