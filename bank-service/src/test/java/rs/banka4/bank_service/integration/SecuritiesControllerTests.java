package rs.banka4.bank_service.integration;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static rs.banka4.bank_service.utils.AssetGenerator.STOCK_EX1_UUID;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import rs.banka4.bank_service.domain.user.client.db.Client;
import rs.banka4.bank_service.integration.generator.PortfolioGenerator;
import rs.banka4.bank_service.integration.generator.UserGenerator;
import rs.banka4.bank_service.repositories.*;
import rs.banka4.bank_service.utils.AssetGenerator;
import rs.banka4.bank_service.utils.ExchangeGenerator;
import rs.banka4.bank_service.utils.ListingGenerator;
import rs.banka4.rafeisen.common.currency.CurrencyCode;
import rs.banka4.rafeisen.common.security.Privilege;
import rs.banka4.testlib.integration.DbEnabledTest;
import rs.banka4.testlib.utils.JwtPlaceholders;

@SpringBootTest
@DbEnabledTest
@AutoConfigureMockMvc
public class SecuritiesControllerTests {

    @Autowired
    private MockMvcTester mvc;
    @Autowired
    private AssetRepository assetRepository;
    @Autowired
    private SecurityRepository securityRepository;
    @Autowired
    private ListingRepository listingRepo;
    @Autowired
    private ExchangeRepository exchangeRepo;
    @Autowired
    private ListingDailyPriceInfoRepository listingHistoryRepo;
    @Autowired
    private PortfolioGenerator portfolioGenerator;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserGenerator userGen;

    @Test
    public void testGetMyPortfolioWithStock() {
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);

        var stock = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        ListingGenerator.makeExampleListings(
            stock.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );
        final var assetOwner = portfolioGenerator.createTestClient();
        portfolioGenerator.createDummyBuyOrder(
            assetOwner,
            stock.get(),
            100,
            BigDecimal.valueOf(1),
            CurrencyCode.USD
        );
        portfolioGenerator.createDummyAssetOwnership(assetOwner, stock.get(), 100, 0, 0);

        String expectedJson = """
            {
              "content": [
                {
                   "assetType": STOCK,
                  "ticker": "EX1",
                  "amount": 100,
                  "price": {"amount": 75.14, "currency": "USD"},
                  "profit": {"amount": 7407.0, "currency": "USD"},
                  "publicAmount": 0
                }
              ],
              "page": {
                "size": 10,
                "number": 0,
                "totalElements": 1,
                "totalPages": 1
              }
            }
            """;
        String jwtToken = "Bearer " + JwtPlaceholders.CLIENT_TOKEN;
        mvc.get()
            .uri("/stock/securities/me")
            .param("page", "0")
            .param("size", "10")
            .header(HttpHeaders.AUTHORIZATION, jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo(expectedJson);
    }

    @Test
    public void testEmptyPortfolio() {
        portfolioGenerator.createTestClient();

        String expectedJson = """
            {
              "content": [],
              "page": {
                "size": 10,
                "number": 0,
                "totalElements": 0,
                "totalPages": 0
              }
            }
            """;

        String jwtToken = "Bearer " + JwtPlaceholders.CLIENT_TOKEN;
        mvc.get()
            .uri("/stock/securities/me")
            .param("page", "0")
            .param("size", "10")
            .header(HttpHeaders.AUTHORIZATION, jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo(expectedJson);
    }

    @Test
    public void testStockZeroProfit() {
        Client client = portfolioGenerator.createTestClient();
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);

        var stock = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        ListingGenerator.makeExampleListings(
            stock.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );
        portfolioGenerator.createDummyAssetOwnership(client, stock.get(), 100, 0, 0);
        portfolioGenerator.createDummyBuyOrderSTOP(
            client,
            stock.get(),
            100,
            BigDecimal.valueOf(75.14),
            CurrencyCode.USD
        );

        String expectedJson = """
            {
              "content": [
                {
                  "ticker": "EX1",
                  "amount": 100,
                  "price": {"amount": 75.14, "currency": "USD"},
                  "profit": {"amount": 0, "currency": "USD"},
                  "publicAmount": 0
                }
              ],
              "page": {
                "size": 10,
                "number": 0,
                "totalElements": 1,
                "totalPages": 1
              }
            }
            """;

        String jwtToken = "Bearer " + JwtPlaceholders.CLIENT_TOKEN;
        mvc.get()
            .uri("/stock/securities/me")
            .param("page", "0")
            .param("size", "10")
            .header(HttpHeaders.AUTHORIZATION, jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo(expectedJson);
    }

    @Test
    public void testStockInProfit() {
        Client client = portfolioGenerator.createTestClient();

        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);
        var stock = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        ListingGenerator.makeExampleListings(
            stock.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );

        portfolioGenerator.createDummyAssetOwnership(client, stock.get(), 99, 1, 0);
        portfolioGenerator.createDummyBuyOrder(
            client,
            stock.get(),
            100,
            BigDecimal.valueOf(40),
            CurrencyCode.USD
        );


        String expectedJson = """
            {
              "content": [
                {
                  "ticker": "EX1",
                  "amount": 100,
                  "price": {"amount": 75.14, "currency": "USD"},
                  "profit": {"amount": 3507.0, "currency": "USD"},
                  "publicAmount": 1
                }
              ],
              "page": {
                "size": 10,
                "number": 0,
                "totalElements": 1,
                "totalPages": 1
              }
            }
            """;

        String jwtToken = "Bearer " + JwtPlaceholders.CLIENT_TOKEN;
        mvc.get()
            .uri("/stock/securities/me")
            .param("page", "0")
            .param("size", "10")
            .header(HttpHeaders.AUTHORIZATION, jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo(expectedJson);
    }

    @Test
    public void testStockNegativeProfit() {
        Client client = portfolioGenerator.createTestClient();

        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);
        var stock = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        ListingGenerator.makeExampleListings(
            stock.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );

        portfolioGenerator.createDummyAssetOwnership(client, stock.get(), 10, 0, 0);
        portfolioGenerator.createDummyBuyOrder(
            client,
            stock.get(),
            10,
            BigDecimal.valueOf(100),
            CurrencyCode.USD
        );

        String expectedJson = """
            {
              "content": [
                {
                  "ticker": "EX1",
                  "amount": 10,
                  "price": {"amount": 75.14, "currency": "USD"},
                  "profit": {"amount": -255.6, "currency": "USD"},
                  "publicAmount": 0
                }
              ],
              "page": {
                "size": 10,
                "number": 0,
                "totalElements": 1,
                "totalPages": 1
              }
            }
            """;

        String jwtToken = "Bearer " + JwtPlaceholders.CLIENT_TOKEN;
        mvc.get()
            .uri("/stock/securities/me")
            .param("page", "0")
            .param("size", "10")
            .header(HttpHeaders.AUTHORIZATION, jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo(expectedJson);
    }

    @Test
    public void testOptionInMoney() {
        Client client = portfolioGenerator.createTestClient();
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);
        var option = assetRepository.findById(AssetGenerator.OPTION_EX1_CALL_UUID);
        var stock = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        ListingGenerator.makeExampleListings(
            stock.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );
        portfolioGenerator.createDummyAssetOwnership(client, option.get(), 1, 0, 0);

        String expectedJson = """
            {
              "content": [
                {
                  "assetType": OPTION,
                  "amount": 1,
                  "price": {"amount":75.14 , "currency": "USD"},
                  "profit": {"amount": -96.36, "currency": "USD"},
                  "publicAmount": 0
                }
              ],
              "page": {
                "size": 10,
                "number": 0,
                "totalElements": 1,
                "totalPages": 1
              }
            }
            """;

        String jwtToken = "Bearer " + JwtPlaceholders.CLIENT_TOKEN;
        mvc.get()
            .uri("/stock/securities/me")
            .param("page", "0")
            .param("size", "10")
            .header(HttpHeaders.AUTHORIZATION, jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo(expectedJson);
    }

    @Test
    public void testOptionOutOfMoney() {
        Client client = portfolioGenerator.createTestClient();
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);
        var option = assetRepository.findById(AssetGenerator.OPTION_EX1_PUT_UUID);
        var stock = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        ListingGenerator.makeExampleListings(
            stock.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );
        portfolioGenerator.createDummyAssetOwnership(client, option.get(), 1, 0, 0);

        String expectedJson = """
            {
              "content": [
                {
                  "amount": 1,
                  "price": {"amount": 75.14, "currency": "USD"},
                  "profit": {"amount": 93.36, "currency": "USD"},
                  "publicAmount": 0
                }
              ],
              "page": {
                "size": 10,
                "number": 0,
                "totalElements": 1,
                "totalPages": 1
              }
            }
            """;

        String jwtToken = "Bearer " + JwtPlaceholders.CLIENT_TOKEN;
        mvc.get()
            .uri("/stock/securities/me")
            .param("page", "0")
            .param("size", "10")
            .header(HttpHeaders.AUTHORIZATION, jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo(expectedJson);
    }


    @Test
    public void testFuturesProfit() {
        Client client = portfolioGenerator.createTestClient();

        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);

        var future = securityRepository.findById(AssetGenerator.FUTURE_CRUDE_OIL_UUID);
        ListingGenerator.makeExampleListings(
            future.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );

        portfolioGenerator.createDummyAssetOwnership(client, future.get(), 2, 0, 0);
        portfolioGenerator.createDummyBuyOrder(
            client,
            future.get(),
            2,
            BigDecimal.valueOf(50),
            CurrencyCode.USD
        );

        String expectedJson = """
            {
              "content": [
                {
                  "assetType": FUTURE,
                  "ticker": "FUT",
                  "amount": 2,
                  "price": {"amount": 80.48, "currency": "USD"},
                  "profit": {"amount": 53960.0, "currency": "USD"},
                  "publicAmount": 0
                }
              ],
              "page": {
                "size": 10,
                "number": 0,
                "totalElements": 1,
                "totalPages": 1
              }
            }
            """;

        String jwtToken = "Bearer " + JwtPlaceholders.CLIENT_TOKEN;
        mvc.get()
            .uri("/stock/securities/me")
            .param("page", "0")
            .param("size", "10")
            .header(HttpHeaders.AUTHORIZATION, jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo(expectedJson);
    }

    @Test
    public void testForexProfit() {
        Client client = portfolioGenerator.createTestClient();

        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);
        var future = securityRepository.findById(AssetGenerator.FOREX_EUR_USD_UUID);
        ListingGenerator.makeExampleListings(
            future.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );

        portfolioGenerator.createDummyAssetOwnership(client, future.get(), 1000, 0, 0);
        portfolioGenerator.createDummyBuyOrder(
            client,
            future.get(),
            1000,
            BigDecimal.valueOf(1.05),
            CurrencyCode.USD
        );

        String expectedJson = """
            {
              "content": [
                {
                "assetType": FOREX_PAIR,
                  "ticker": "USD/EUR",
                  "amount": 1000,
                  "price": {"amount": 83.95, "currency": "USD"},
                  "profit": {"amount": 82893.0, "currency": "USD"},
                  "publicAmount": 0
                }
              ],
              "page": {
                "size": 10,
                "number": 0,
                "totalElements": 1,
                "totalPages": 1
              }
            }
            """;

        String jwtToken = "Bearer " + JwtPlaceholders.CLIENT_TOKEN;
        mvc.get()
            .uri("/stock/securities/me")
            .param("page", "0")
            .param("size", "10")
            .header(HttpHeaders.AUTHORIZATION, jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo(expectedJson);
    }

    @Test
    public void testMultipleHoldings() {
        Client client = portfolioGenerator.createTestClient();

        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);
        var stockA = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        ListingGenerator.makeExampleListings(
            stockA.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );
        portfolioGenerator.createDummyAssetOwnership(client, stockA.orElseThrow(), 100, 0, 0);
        portfolioGenerator.createDummyBuyOrderSTOP(
            client,
            stockA.get(),
            100,
            BigDecimal.valueOf(40),
            CurrencyCode.USD
        );

        var stockB = securityRepository.findById(AssetGenerator.STOCK_EX2_UUID);
        ListingGenerator.makeExampleListings(
            stockB.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );
        portfolioGenerator.createDummyAssetOwnership2(client, stockB.get(), 10, 0, 0);
        portfolioGenerator.createDummyBuyOrder2(
            client,
            stockB.orElseThrow(),
            10,
            BigDecimal.valueOf(10),
            CurrencyCode.USD
        );

        String expectedJson = """
            {
              "content": [
                {
                  "ticker": "EX1",
                  "amount": 100,
                  "price": {"amount": 75.14, "currency": "USD"},
                  "profit": {"amount": 3514.0, "currency": "USD"},
                  "publicAmount": 0
                },
                {
                  "ticker": "EX2",
                  "amount": 10,
                  "price": {"amount": 46.69, "currency": "USD"},
                  "profit": {"amount": 359.9, "currency": "USD"},
                  "publicAmount": 0
                }
              ],
              "page": {
                "size": 10,
                "number": 0,
                "totalElements": 2,
                "totalPages": 1
              }
            }
            """;

        String jwtToken = "Bearer " + JwtPlaceholders.CLIENT_TOKEN;
        mvc.get()
            .uri("/stock/securities/me")
            .param("page", "0")
            .param("size", "10")
            .header(HttpHeaders.AUTHORIZATION, jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo(expectedJson);
    }

    @Test
    public void testPartialSells() {
        Client client = portfolioGenerator.createTestClient();
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);
        var stock = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        ListingGenerator.makeExampleListings(
            stock.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );
        portfolioGenerator.createDummyAssetOwnership(client, stock.get(), 150, 0, 0);
        portfolioGenerator.createDummyBuyOrder(
            client,
            stock.get(),
            200,
            BigDecimal.valueOf(20),
            CurrencyCode.USD
        );
        portfolioGenerator.createDummySellOrder(
            client,
            stock.get(),
            50,
            BigDecimal.valueOf(25),
            CurrencyCode.USD
        );

        String expectedJson = """
            {
              "content": [
                {
                  "ticker": "EX1",
                  "amount": 150,
                  "price": {"amount": 75.14, "currency": "USD"},
                  "profit": {"amount": 8265.75, "currency": "USD"},
                  "publicAmount": 0
                }
              ],
              "page": {
                "size": 10,
                "number": 0,
                "totalElements": 1,
                "totalPages": 1
              }
            }
            """;

        String jwtToken = "Bearer " + JwtPlaceholders.CLIENT_TOKEN;
        mvc.get()
            .uri("/stock/securities/me")
            .param("page", "0")
            .param("size", "10")
            .header(HttpHeaders.AUTHORIZATION, jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo(expectedJson);
    }

    @Test
    public void testPartialSellsNotSoldAll() {
        Client client = portfolioGenerator.createTestClient();
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);
        var stock = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        ListingGenerator.makeExampleListings(
            stock.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );
        portfolioGenerator.createDummyAssetOwnership(client, stock.get(), 150, 0, 0);
        portfolioGenerator.createDummyBuyOrder(
            client,
            stock.get(),
            200,
            BigDecimal.valueOf(20),
            CurrencyCode.USD
        );
        var sellOrder =
            portfolioGenerator.createDummySellOrder(
                client,
                stock.get(),
                50,
                BigDecimal.valueOf(25),
                CurrencyCode.USD
            );
        sellOrder.setRemainingPortions(10);
        sellOrder.setDone(false);
        orderRepository.save(sellOrder);
        String expectedJson = """
            {
              "content": [
                {
                  "ticker": "EX1",
                  "amount": 150,
                  "price": {"amount": 75.14, "currency": "USD"},
                  "profit": {"amount": 8816.8, "currency": "USD"},
                  "publicAmount": 0
                }
              ],
              "page": {
                "size": 10,
                "number": 0,
                "totalElements": 1,
                "totalPages": 1
              }
            }
            """;

        String jwtToken = "Bearer " + JwtPlaceholders.CLIENT_TOKEN;
        mvc.get()
            .uri("/stock/securities/me")
            .param("page", "0")
            .param("size", "10")
            .header(HttpHeaders.AUTHORIZATION, jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo(expectedJson);
    }

    @Test
    public void testTotalProfitCalculation() {
        Client client = portfolioGenerator.createTestClient();
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);
        var stock = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        ListingGenerator.makeExampleListings(
            stock.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );
        var stock2 = securityRepository.findById(AssetGenerator.STOCK_EX2_UUID);
        ListingGenerator.makeExampleListings(
            stock2.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );
        portfolioGenerator.createDummyAssetOwnership(client, stock.get(), 150, 0, 0);
        portfolioGenerator.createDummyAssetOwnership2(client, stock2.get(), 150, 0, 0);
        portfolioGenerator.createDummyBuyOrder(
            client,
            stock.get(),
            200,
            BigDecimal.valueOf(20),
            CurrencyCode.USD
        );
        portfolioGenerator.createDummyBuyOrder2(
            client,
            stock2.get(),
            50,
            BigDecimal.valueOf(20),
            CurrencyCode.USD
        );
        portfolioGenerator.createDummySellOrder(
            client,
            stock.get(),
            50,
            BigDecimal.valueOf(25),
            CurrencyCode.USD
        );

        String expectedJson = """
                {
                  "amount":9593.25,
                  "currency":"USD"
                }
            """;

        String jwtToken = "Bearer " + JwtPlaceholders.CLIENT_TOKEN;
        mvc.get()
            .uri("/stock/securities/profit")
            .header(HttpHeaders.AUTHORIZATION, jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo(expectedJson);
    }

    @Test
    public void testMyTaxCalculation() {
        Client client = portfolioGenerator.createTestClient();

        portfolioGenerator.createDummyTax(client);
        portfolioGenerator.createDummyTaxEur(client);

        String expectedJson = """
                {
                "paidTaxThisYear":1200.0,
                "unpaidTaxThisMonth":6020.281359906214,
                "currency":"RSD"
                }
            """;

        String jwtToken = "Bearer " + JwtPlaceholders.CLIENT_TOKEN;
        mvc.get()
            .uri("/stock/securities/tax")
            .header(HttpHeaders.AUTHORIZATION, jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo(expectedJson);
    }

    @Test
    public void testMyTaxCalculationNoTax() {
        Client client = portfolioGenerator.createTestClient();

        String expectedJson = """
                {
                "paidTaxThisYear":0,
                "unpaidTaxThisMonth":0,
                "currency":"RSD"
                }
            """;

        String jwtToken = "Bearer " + JwtPlaceholders.CLIENT_TOKEN;
        mvc.get()
            .uri("/stock/securities/tax")
            .header(HttpHeaders.AUTHORIZATION, jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo(expectedJson);
    }

    @Test
    void testBankProfitEndpoint() {
        Client client = portfolioGenerator.createTestClientDiffIf();
        var acts = portfolioGenerator.createTestActuary();
        var clients =
            acts.stream()
                .map(act -> {
                    var u = userRepository.findById(act.getUserId());
                    u.get()
                        .setPrivileges(List.of(Privilege.AGENT));
                    return userRepository.save(u.get());
                })
                .toList();
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);
        var stock = securityRepository.findById(STOCK_EX1_UUID);
        ListingGenerator.makeExampleListings(
            stock.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );
        var orderB =
            portfolioGenerator.createDummyBuyOrder(
                client,
                stock.get(),
                100,
                BigDecimal.valueOf(40),
                CurrencyCode.EUR
            );
        var orderS =
            portfolioGenerator.createDummySellOrder(
                client,
                stock.get(),
                200,
                BigDecimal.valueOf(50),
                CurrencyCode.EUR
            );
        orderS.setUser(clients.get(0));
        orderB.setUser(clients.get(0));
        orderRepository.saveAllAndFlush(List.of(orderB, orderS));
        String jwtToken = "Bearer " + JwtPlaceholders.ADMIN_EMPLOYEE_TOKEN;
        userGen.createEmployee(
            e -> e.id(JwtPlaceholders.CLIENT_ID)
                .email("bala@bla.com")
                .username("bala")
        );
        String json = """
            {
              "content": [
                {
                  "profit": {
                    "amount": 116747.94841735054008,
                    "currency": "RSD"
                  },
                  "name": "John",
                  "surname": "Doe",
                  "position": "Developer"
                },
                {
                  "profit": {
                    "amount": 0,
                    "currency": "RSD"
                  },
                  "name": "John",
                  "surname": "Doe",
                  "position": "Developer"
                }
              ],
              "page": {
                "size": 10,
                "number": 0,
                "totalElements": 2,
                "totalPages": 1
              }
            }""";

        mvc.get()
            .uri("/stock/securities/bank/profit")
            .header(HttpHeaders.AUTHORIZATION, jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo(json);
    }

}
