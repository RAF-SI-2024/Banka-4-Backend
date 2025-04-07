package rs.banka4.stock_service.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import rs.banka4.stock_service.domain.listing.db.Listing;
import rs.banka4.stock_service.domain.listing.dtos.ListingFilterDto;
import rs.banka4.stock_service.domain.listing.dtos.SecurityType;
import rs.banka4.stock_service.domain.listing.specificaion.ListingSpecification;
import rs.banka4.stock_service.domain.security.stock.db.Stock;
import rs.banka4.stock_service.repositories.AssetRepository;
import rs.banka4.stock_service.repositories.ExchangeRepository;
import rs.banka4.stock_service.repositories.ListingDailyPriceInfoRepository;
import rs.banka4.stock_service.repositories.ListingRepository;
import rs.banka4.stock_service.repositories.SecurityRepository;
import rs.banka4.stock_service.utils.AssetGenerator;
import rs.banka4.stock_service.utils.ExchangeGenerator;
import rs.banka4.stock_service.utils.ListingGenerator;
import rs.banka4.testlib.integration.DbEnabledTest;
import rs.banka4.testlib.utils.JwtPlaceholders;

@SpringBootTest
@DbEnabledTest
@AutoConfigureMockMvc
public class ListingTests {
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

    @Test
    public void test_getListingDetails_basic() {
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);

        var ex1 = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        ListingGenerator.makeExampleListings(
            ex1.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );

        mvc.get()
            /* TODO(arsen): swap with security id? */
            .uri("/listings/{stockId}", AssetGenerator.STOCK_EX1_UUID)
            .header("Authorization", "Bearer " + JwtPlaceholders.ADMIN_EMPLOYEE_TOKEN)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                {
                  "securityType": "STOCK",
                  "name": "Example One™",
                  "ticker": "EX1",
                  "change": 21.96,
                  "price": 66.40,
                  "outstandingShares": 325000,
                  "dividendYield": 0.05
                }
                """);
    }

    @Test
    public void test_getListingOptions_basic() {
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);

        var ex1 = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        ListingGenerator.makeExampleListings(
            ex1.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );

        final var settlementDate =
            OffsetDateTime.now()
                .plusYears(2);

        mvc.get()
            /* TODO(arsen): swap with security id? */
            .uri("/listings/options/{stockId}", AssetGenerator.STOCK_EX1_UUID)
            .header("Authorization", "Bearer " + JwtPlaceholders.ADMIN_EMPLOYEE_TOKEN)
            .param("settlementDate", settlementDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            /* IDs are: OPTION_EX1_{PUT,CALL}{,2}_UUID in AssetGenerator */
            .isLenientlyEqualTo("""
                [
                  {
                    "callsId": "b5b843a4-c90c-4a14-8bdf-06da1b543f97",
                    "callsLastPrice": 0,
                    "callsChange": 0,
                    "callsImpliedVolatility": 337.8,
                    "callsVolume": 0,
                    "callsOpenInterest": 445,
                    "callsPremium":
                    {
                        "amount":1.50,
                        "currency":"USD"
                    },
                    "strike": 170.00,
                    "putsId": "65d965c1-0d7c-4506-a424-4fe81a1f6357",
                    "putsLastPrice": 0,
                    "putsChange": 0,
                    "putsImpliedVolatility": 412.5,
                    "putsVolume": 0,
                    "putsOpenInterest": 565,
                    "putsPremium":
                    {
                        "amount":1.50,
                        "currency":"USD"
                    }
                  },

                  {
                    "callsId": "964919c6-2e54-4de2-8115-24f44a5c2ea5",
                    "callsLastPrice": 0,
                    "callsChange": 0,
                    "callsImpliedVolatility": 122.4,
                    "callsVolume": 0,
                    "callsOpenInterest": 914,
                    "callsPremium":
                    {
                        "amount":1.50,
                        "currency":"USD"
                    },
                    "strike": 140.00,
                    "putsId": "18995f03-540e-455e-b44c-ce819c00e562",
                    "putsLastPrice": 0,
                    "putsChange": 0,
                    "putsImpliedVolatility": 338.9,
                    "putsVolume": 0,
                    "putsOpenInterest": 878,
                    "putsPremium":
                    {
                        "amount":1.50,
                        "currency":"USD"
                    }
                  }
                ]
                """);
    }

    /**
     * Verify that getPriceChanges returns all changes from oldest to newest.
     */
    @Test
    public void test_getPriceChanges_basic() {
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);

        var ex1 = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        ListingGenerator.makeExampleListings(
            ex1.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );

        mvc.get()
            .uri("/listings/" + AssetGenerator.STOCK_EX1_UUID + "/priceChange")
            .header("Authorization", "Bearer " + JwtPlaceholders.ADMIN_EMPLOYEE_TOKEN)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                [
                  {
                    "price": 28.74
                  },
                  {
                    "price": 5.90
                  },
                  {
                    "price": 12.18
                  },
                  {
                    "price": 19.21
                  },
                  {
                    "price": 44.44
                  }
                ]
                """)
            .extractingPath("$..date")
            .asArray()
            .isSortedAccordingTo(
                Comparator.comparing(
                    maybeDate -> OffsetDateTime.parse(
                        (String) maybeDate,
                        DateTimeFormatter.ISO_OFFSET_DATE_TIME
                    )
                )
            );
    }

    /**
     * Verify that getListings returns all up-to-date listings. Given that we only generate a bunch
     * of listings for {@link AssetGenerator#STOCK_EX1_UUID} currently, that will be only one
     * element.
     */
    @Test
    public void test_getListings_no_filter_only_one_stock() {
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);

        var ex1 = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        ListingGenerator.makeExampleListings(
            ex1.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );

        mvc.get()
            .uri("/listings?page=0&size=2")
            .header("Authorization", "Bearer " + JwtPlaceholders.ADMIN_EMPLOYEE_TOKEN)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                {
                  "content": [
                    {
                      "name": "Example One™",
                      "ticker": "EX1",
                      "volume": 0,
                      "change": 21.96,
                      "price": 66.40
                    }
                  ],
                  "page": {"totalElements": 1}
                }
                """)
            .extractingPath("$.content")
            .asArray()
            .hasSize(1);
    }

    /**
     * Filter by searchName only.
     */
    @Test
    public void test_getListings_filterByName() {
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);

        var ex1 = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        ListingGenerator.makeExampleListings(
            ex1.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );

        mvc.get()
            .uri("/listings?searchName={name}&page=0&size=2", "Example O")
            .header("Authorization", "Bearer " + JwtPlaceholders.ADMIN_EMPLOYEE_TOKEN)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                {
                  "content": [
                    {
                      "name": "Example One™",
                      "ticker": "EX1"
                    }
                  ],
                  "page": {"totalElements": 1}
                }
                """);
    }

    /**
     * Filter by searchName only and name doesn't exist.
     */
    @Test
    public void test_getListings_filterByName_empty() {
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);

        var ex1 = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        ListingGenerator.makeExampleListings(
            ex1.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );

        mvc.get()
            .uri("/listings?searchName={name}&page=0&size=2", "BlaBla")
            .header("Authorization", "Bearer " + JwtPlaceholders.ADMIN_EMPLOYEE_TOKEN)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .extractingPath("$.content")
            .asArray()
            .isEmpty();
    }

    /**
     * Filter by searchTicker only.
     */
    @Test
    public void test_getListings_filterByTicker() {
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);

        var ex1 = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        ListingGenerator.makeExampleListings(
            ex1.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );

        mvc.get()
            .uri("/listings?searchTicker=EX1&page=0&size=2")
            .header("Authorization", "Bearer " + JwtPlaceholders.ADMIN_EMPLOYEE_TOKEN)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                {
                  "content": [
                    {
                      "name": "Example One™",
                      "ticker": "EX1"
                    }
                  ],
                  "page": {"totalElements": 1}
                }
                """);
    }

    /**
     * Filter by searchTicker and ticker doesn't exist.
     */
    @Test
    public void test_getListings_filterByTicker_empty() {
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);

        var ex1 = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        ListingGenerator.makeExampleListings(
            ex1.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );

        mvc.get()
            .uri("/listings?searchTicker=BLA&page=0&size=2")
            .header("Authorization", "Bearer " + JwtPlaceholders.ADMIN_EMPLOYEE_TOKEN)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .extractingPath("$.content")
            .asArray()
            .isEmpty();
    }

    /**
     * Filter by ask price range.
     */
    @Test
    public void test_getListings_filterByPriceRange() {
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);

        var ex1 = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        ListingGenerator.makeExampleListings(
            ex1.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );

        mvc.get()
            .uri("/listings?askMin=60.00&askMax=70.00&page=0&size=2")
            .header("Authorization", "Bearer " + JwtPlaceholders.ADMIN_EMPLOYEE_TOKEN)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                {
                  "content": [
                    {
                      "price": 66.40
                    }
                  ],
                  "page": {"totalElements": 1}
                }
                """);
    }

    /**
     * Filter by bid price range.
     */
    @Test
    public void test_getListings_filterByBidRange() {
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);

        var ex1 = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        ListingGenerator.makeExampleListings(
            ex1.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );

        mvc.get()
            .uri("/listings?bidMin=60.00&bidMax=80.0&page=0&size=2")
            .header("Authorization", "Bearer " + JwtPlaceholders.ADMIN_EMPLOYEE_TOKEN)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                {
                  "content": [
                    {
                      "price": 66.40
                    }
                  ],
                  "page": {"totalElements": 1}
                }
                """);
    }

    /**
     * Filter by bid price out of range.
     */
    @Test
    public void test_getListings_filterByBidRange_empty() {
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);

        var ex1 = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        ListingGenerator.makeExampleListings(
            ex1.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );

        mvc.get()
            .uri("/listings?bidMin=90.00&bidMax=100.0&page=0&size=2")
            .header("Authorization", "Bearer " + JwtPlaceholders.ADMIN_EMPLOYEE_TOKEN)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .extractingPath("$.content")
            .asArray()
            .isEmpty();
    }

    /**
     * Filter by exchange prefix.
     */
    @Test
    public void test_getListings_filterByExchangePrefix() {
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);

        var ex1 = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        ListingGenerator.makeExampleListings(
            ex1.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );

        mvc.get()
            .uri("/listings?exchangePrefix=Nasdaq&page=0&size=2")
            .header("Authorization", "Bearer " + JwtPlaceholders.ADMIN_EMPLOYEE_TOKEN)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                {
                  "content": [
                    {
                      "name": "Example One™",
                      "ticker": "EX1"
                    }
                  ],
                  "page": {"totalElements": 1}
                }
                """);
    }

    /**
     * Filter by volume (order count). This example assumes that the generated listing has a
     * computed volume.
     */
    @Test
    public void test_getListings_filterByVolume() {
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);

        var ex1 = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        ListingGenerator.makeExampleListings(
            ex1.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );

        mvc.get()
            .uri("/listings?volumeMin=0&volumeMax=10&page=0&size=2")
            .header("Authorization", "Bearer " + JwtPlaceholders.ADMIN_EMPLOYEE_TOKEN)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                {
                  "content": [
                    {
                      "name": "Example One™",
                      "ticker": "EX1",
                      "volume": 0
                    }
                  ],
                  "page": {"totalElements": 1}
                }
                """);
    }

    /**
     * Filter by settlement date for a FUTURE asset.
     */
    @Test
    public void test_getListings_filterBySettlementDate() {
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);

        var futureAsset = securityRepository.findById(AssetGenerator.FUTURE_CRUDE_OIL_UUID);
        ListingGenerator.makeExampleListings(
            futureAsset.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );

        String from =
            OffsetDateTime.parse("2026-05-02T16:53:41.428942Z")
                .minusDays(1)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String to =
            OffsetDateTime.parse("2026-05-02T16:53:41.428942Z")
                .plusMonths(12)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        mvc.get()
            .uri(
                "/listings?settlementDateFrom="
                    + from
                    + "&settlementDateTo="
                    + to
                    + "&securityType=FUTURE&page=0&size=2"
            )
            .header("Authorization", "Bearer " + JwtPlaceholders.ADMIN_EMPLOYEE_TOKEN)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                {
                  "content": [
                    {
                      "name": "Crude Oil",
                      "ticker": "FUT"
                    }
                  ],
                  "page": {"totalElements": 1}
                }
                """);
    }

    /**
     * Combination filter: use several parameters together.
     */
    @Test
    public void test_getListings_combination() {
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);

        var ex1 = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        ListingGenerator.makeExampleListings(
            ex1.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );

        mvc.get()
            .uri(
                "/listings?searchName=Example&askMin=60.00&askMax=70.00&exchangePrefix=Nasdaq&page=0&size=2"
            )
            .header("Authorization", "Bearer " + JwtPlaceholders.ADMIN_EMPLOYEE_TOKEN)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                {
                  "content": [
                    {
                      "name": "Example One™",
                      "ticker": "EX1",
                      "volume": 0,
                      "change": 21.96,
                      "price": 66.40
                    }
                  ],
                  "page": {"totalElements": 1}
                }
                """);
    }

    /**
     * Invalid price range (askMin greater than askMax) should return no listings.
     */
    @Test
    public void test_getListings_invalidPriceRange() {
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);

        var ex1 = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        ListingGenerator.makeExampleListings(
            ex1.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );

        mvc.get()
            .uri("/listings?askMin=110.00&askMax=90.00&page=0&size=2")
            .header("Authorization", "Bearer " + JwtPlaceholders.ADMIN_EMPLOYEE_TOKEN)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .extractingPath("$.content")
            .asArray()
            .isEmpty();
    }

    /**
     * Invalid settlement date range (from date after to date) should return no listings.
     */
    @Test
    public void test_getListings_invalidSettlementDateRange() {
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);

        var futureAsset = securityRepository.findById(AssetGenerator.FUTURE_CRUDE_OIL_UUID);
        ListingGenerator.makeExampleListings(
            futureAsset.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );

        String from =
            OffsetDateTime.now()
                .plusMonths(14)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String to =
            OffsetDateTime.now()
                .plusMonths(12)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        mvc.get()
            .uri(
                "/listings?settlementDateFrom="
                    + from
                    + "&settlementDateTo="
                    + to
                    + "&securityType=FUTURE&page=0&size=2"
            )
            .header("Authorization", "Bearer " + JwtPlaceholders.ADMIN_EMPLOYEE_TOKEN)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .extractingPath("$.content")
            .asArray()
            .isEmpty();
    }

    /**
     * Sorting test: sort by PRICE ascending. (This test requires at least two listings with
     * different ask prices.)
     */
    @Test
    public void test_getListings_sortByPriceAsc() {
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);

        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);
        var ex1 = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        var ex2 = securityRepository.findById(AssetGenerator.STOCK_EX2_UUID);

        ListingGenerator.makeExampleListings(
            ex1.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );
        ListingGenerator.makeExampleListings(
            ex2.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );

        mvc.get()
            .uri("/listings?sortBy=PRICE&sortDirection=ASC&page=0&size=10")
            .header("Authorization", "Bearer " + JwtPlaceholders.ADMIN_EMPLOYEE_TOKEN)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .extractingPath("$.content")
            .asArray()
            .isSortedAccordingTo(new Comparator<Object>() {
                @SuppressWarnings("unchecked")
                @Override
                public int compare(Object o1, Object o2) {
                    Map<String, Object> map1 = (Map<String, Object>) o1;
                    Map<String, Object> map2 = (Map<String, Object>) o2;
                    BigDecimal price1 =
                        new BigDecimal(
                            map1.get("price")
                                .toString()
                        );
                    BigDecimal price2 =
                        new BigDecimal(
                            map2.get("price")
                                .toString()
                        );
                    return price1.compareTo(price2);
                }
            });
    }

    /**
     * Test that empty strings for text filters do not trigger filtering.
     */
    @Test
    public void test_getListings_emptyStringsIgnored() {
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);
        var ex1 = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        ListingGenerator.makeExampleListings(
            ex1.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );

        mvc.get()
            .uri("/listings?searchName=&searchTicker=&page=0&size=2")
            .header("Authorization", "Bearer " + JwtPlaceholders.ADMIN_EMPLOYEE_TOKEN)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                {
                  "page": {"totalElements": 1}
                }
                """);
    }

    /**
     * Test that in client mode (isClient=true) the specification returns the listing even when
     * securityType is null—client mode defaults to allowing STOCK and FUTURE.
     */
    @Test
    public void test_listingSpecification_clientMode_defaultTypes() {
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);
        var ex1 = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        var fut = securityRepository.findById(AssetGenerator.FUTURE_CRUDE_OIL_UUID);
        var for1 = securityRepository.findById(AssetGenerator.FOREX_EUR_USD_UUID);
        ListingGenerator.makeExampleListings(
            ex1.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );
        ListingGenerator.makeExampleListings(
            fut.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );
        ListingGenerator.makeExampleListings(
            for1.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );

        ListingFilterDto filter = new ListingFilterDto();

        Specification<Listing> spec = ListingSpecification.getSpecification(filter, true);
        List<Listing> results = listingRepo.findAll(spec);

        assertThat(results).hasSize(2);

    }

    /**
     * Test that in client mode (isClient=true) the specification returns the listing with
     * securityType search.
     */
    @Test
    public void test_listingSpecification_clientMode_TypeSearch_defaultTypes() {
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);
        var ex1 = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        var fut = securityRepository.findById(AssetGenerator.FUTURE_CRUDE_OIL_UUID);
        ListingGenerator.makeExampleListings(
            ex1.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );
        ListingGenerator.makeExampleListings(
            fut.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );

        ListingFilterDto filter = new ListingFilterDto();
        filter.setSecurityType(SecurityType.STOCK);

        Specification<Listing> spec = ListingSpecification.getSpecification(filter, true);
        List<Listing> results = listingRepo.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(
            results.get(0)
                .getSecurity()
                .getClass()
        ).isEqualTo(Stock.class);
    }

    /**
     * Test that in client mode (isClient=true) the specification returns the listing even when
     * securityType is not searchable by client it ignores the search.
     */
    @Test
    public void test_listingSpecification_clientMode_search_ignores() {
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);
        var ex1 = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        var fut = securityRepository.findById(AssetGenerator.FUTURE_CRUDE_OIL_UUID);
        var for1 = securityRepository.findById(AssetGenerator.FOREX_EUR_USD_UUID);
        ListingGenerator.makeExampleListings(
            ex1.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );
        ListingGenerator.makeExampleListings(
            fut.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );
        ListingGenerator.makeExampleListings(
            for1.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );

        ListingFilterDto filter = new ListingFilterDto();
        filter.setSecurityType(SecurityType.FOREX_PAIR);

        Specification<Listing> spec = ListingSpecification.getSpecification(filter, true);
        List<Listing> results = listingRepo.findAll(spec);

        assertThat(results).hasSize(2);

    }

    /**
     * Test that in client mode (isClient=true) getListings endpoint returns the listing even when
     * securityType is null—client mode defaults to allowing STOCK and FUTURE.
     */
    @Test
    public void test_getListings_clientMode_defaultTypes() {
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);
        var ex1 = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        var fut = securityRepository.findById(AssetGenerator.FUTURE_CRUDE_OIL_UUID);
        var for1 = securityRepository.findById(AssetGenerator.FOREX_EUR_USD_UUID);
        ListingGenerator.makeExampleListings(
            ex1.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );
        ListingGenerator.makeExampleListings(
            fut.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );
        ListingGenerator.makeExampleListings(
            for1.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );

        mvc.get()
            .uri("/listings?searchName=&searchTicker=&page=0&size=2")
            .header("Authorization", "Bearer " + JwtPlaceholders.CLIENT_TOKEN)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                {
                  "page": {"totalElements": 2}
                }
                """);

    }

    /**
     * Test that in client mode (isClient=true) getListings endpoint returns the listing with
     * securityType search.
     */
    @Test
    public void test_getListings_clientMode_TypeSearch_defaultTypes() {
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);
        var ex1 = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        var fut = securityRepository.findById(AssetGenerator.FUTURE_CRUDE_OIL_UUID);
        ListingGenerator.makeExampleListings(
            ex1.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );
        ListingGenerator.makeExampleListings(
            fut.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );

        mvc.get()
            .uri(
                "/listings?searchName=Example&askMin=60.00&askMax=70.00&exchangePrefix=Nasdaq&page=0&size=2"
            )
            .header("Authorization", "Bearer " + JwtPlaceholders.CLIENT_TOKEN)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                {
                  "content": [
                    {
                      "name": "Example One™",
                      "ticker": "EX1",
                      "volume": 0,
                      "change": 21.96,
                      "price": 66.40
                    }
                  ],
                  "page": {"totalElements": 1}
                }
                """);
    }

    /**
     * Test that in client mode (isClient=true) getListings endpoint returns the listing even when
     * securityType is not searchable by client it ignores the search.
     */
    @Test
    public void test_getListings_clientMode_search_ignores() {
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);
        var ex1 = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        var fut = securityRepository.findById(AssetGenerator.FUTURE_CRUDE_OIL_UUID);
        var for1 = securityRepository.findById(AssetGenerator.FOREX_EUR_USD_UUID);
        ListingGenerator.makeExampleListings(
            ex1.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );
        ListingGenerator.makeExampleListings(
            fut.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );
        ListingGenerator.makeExampleListings(
            for1.orElseThrow(),
            ber1,
            listingRepo,
            listingHistoryRepo
        );

        mvc.get()
            .uri("/listings?searchName=&searchTicker=&page=0&size=2")
            .header("Authorization", "Bearer " + JwtPlaceholders.CLIENT_TOKEN)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                {
                  "page": {"totalElements": 2}
                }
                """);

    }
}
