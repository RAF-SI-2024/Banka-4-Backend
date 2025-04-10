package rs.banka4.stock_service.integration.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import rs.banka4.rafeisen.common.currency.CurrencyCode;
import rs.banka4.stock_service.domain.actuaries.db.ActuaryInfo;
import rs.banka4.stock_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.stock_service.domain.exchanges.db.Exchange;
import rs.banka4.stock_service.domain.listing.db.Listing;
import rs.banka4.stock_service.domain.security.stock.db.Stock;
import rs.banka4.stock_service.repositories.*;
import rs.banka4.stock_service.utils.AssetGenerator;
import rs.banka4.testlib.integration.DbEnabledTest;
import rs.banka4.testlib.utils.JwtPlaceholders;

@SpringBootTest
@DbEnabledTest
@AutoConfigureMockMvc
public class OrderControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @Autowired
    private AssetRepository assetRepo;
    @Autowired
    private ActuaryRepository actuaryRepository;
    @Autowired
    private ListingRepository listingRepository;
    @Autowired
    private ExchangeRepository exchangeRepository;

    private UUID stockId;

    @BeforeEach
    void setUp() {
        assetRepo.deleteAll();
        listingRepository.deleteAll();

        assetRepo.saveAll(AssetGenerator.makeExampleAssets());
        stockId = AssetGenerator.STOCK_EX1_UUID;

        Stock stock =
            (Stock) assetRepo.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        Exchange exchange = TestDataFactory.buildExchange();
        exchangeRepository.save(exchange);

        Listing listing = TestDataFactory.buildListing(stock, exchange);
        listingRepository.save(listing);
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        actuaryRepository.save(
            new ActuaryInfo(
                JwtPlaceholders.CLIENT_ID,
                true,
                new MonetaryAmount(BigDecimal.valueOf(9999), CurrencyCode.RSD),
                new MonetaryAmount(BigDecimal.ZERO, CurrencyCode.RSD)
            )
        );
        String jwt = "Bearer " + JwtPlaceholders.CLIENT_TOKEN;

        String payload = """
            {
              "assetId": "%s",
              "direction": "BUY",
              "quantity": 10,
              "limitValue": null,
              "stopValue": null,
              "allOrNothing": false,
              "margin": false,
              "accountId": "%s"
            }
            """.formatted(AssetGenerator.STOCK_EX1_UUID, TestDataFactory.ACCOUNT_ID.toString());

        mvc.post()
            .uri("/orders")
            .header("Authorization", jwt)
            .contentType("application/json")
            .content(payload)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .satisfies(json -> {
                assertThat(json.toString()).contains("\"id\":");
                assertThat(json.toString()).contains(
                    "\"userId\":\"a4bf370e-2129-4116-9243-0c4ead0fe43e\""
                );
                assertThat(json.toString()).contains("\"assetTicker\":\"EX1\"");
                assertThat(json.toString()).contains("\"orderType\":\"MARKET\"");
                assertThat(json.toString()).contains("\"direction\":\"BUY\"");
                assertThat(json.toString()).contains("\"quantity\":10");
            });
    }

    @Test
    void shouldCalculateAveragePriceSuccessfully() {
        String jwt = "Bearer " + JwtPlaceholders.CLIENT_TOKEN;

        String payload = """
            {
              "assetId": "%s",
              "direction": "BUY",
              "amount": 10
            }
            """.formatted(stockId);

        mvc.post()
            .uri("/orders/calculate-average-price")
            .header("Authorization", jwt)
            .contentType("application/json")
            .content(payload)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .satisfies(json -> {
                assertThat(json.toString()).contains("orderType");
                assertThat(json.toString()).contains("approximatePrice");
                assertThat(json.toString()).contains("quantity");
            });
    }

    @Test
    void shouldFailCalculateAveragePriceWithInvalidPayload() {
        String jwt = "Bearer " + JwtPlaceholders.CLIENT_TOKEN;

        String payload = """
            {
              "amount": 10
            }
            """;

        mvc.post()
            .uri("/orders/calculate-average-price")
            .header("Authorization", jwt)
            .contentType("application/json")
            .content(payload)
            .assertThat()
            .hasStatus(HttpStatus.BAD_REQUEST);
    }
}
