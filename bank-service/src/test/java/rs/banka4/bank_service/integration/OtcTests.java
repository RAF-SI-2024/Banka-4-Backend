package rs.banka4.bank_service.integration;

import static org.junit.jupiter.api.Assertions.*;
import static rs.banka4.bank_service.generator.OtcRequestGen.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import rs.banka4.bank_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.bank_service.domain.assets.db.AssetOwnership;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;
import rs.banka4.bank_service.domain.trading.db.OtcRequest;
import rs.banka4.bank_service.domain.trading.db.RequestStatus;
import rs.banka4.bank_service.domain.trading.db.dtos.OtcRequestCreateDto;
import rs.banka4.bank_service.domain.trading.db.dtos.OtcRequestUpdateDto;
import rs.banka4.bank_service.domain.trading.utill.BankRoutingNumber;
import rs.banka4.bank_service.integration.generator.UserGenerator;
import rs.banka4.bank_service.repositories.*;
import rs.banka4.bank_service.service.impl.OtcRequestExpiryService;
import rs.banka4.bank_service.utils.AssetGenerator;
import rs.banka4.rafeisen.common.currency.CurrencyCode;
import rs.banka4.testlib.integration.DbEnabledTest;
import rs.banka4.testlib.utils.JwtPlaceholders;

/**
 * Integration tests for OTC requests endpoints.
 *
 * <p>
 * This test class covers endpoints for retrieving the authenticated user's OTC requests, both all
 * and unread requests, as well as rejecting, updating, and creating new OTC requests.
 */
@SpringBootTest
@DbEnabledTest
@AutoConfigureMockMvc
public class OtcTests {

    @Autowired
    private MockMvcTester mvc;

    @Autowired
    private ObjectMapper objMapper;

    @Autowired
    private OtcRequestRepository otcRequestRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private SecurityRepository securityRepository;
    @Autowired
    private OptionsRepository optionsRepository;

    @Autowired
    private UserGenerator userGen;

    @Autowired
    private AssetOwnershipRepository assetOwnershipRepository;
    @Autowired
    ListingRepository listingRepository;
    @Autowired
    private ListingDailyPriceInfoRepository listingHistoryRepo;
    @Autowired
    private ExchangeRepository exchangeRepository;


    /**
     * Tests that the /otc/me endpoint returns a list of OTC requests for the authenticated user.
     * <p>
     * This test uses dummy OTC requests (one created by createDummyOtcRequest and one by
     * createDummyOtcRequestMeRead) and asserts that the returned JSON matches the expected JSON.
     */
    @Test
    public void testGetMyRequests() {
        OtcRequest dummyRequest =
            createDummyOtcRequest(
                assetRepository,
                securityRepository,
                otcRequestRepository,
                listingRepository,
                listingHistoryRepo,
                exchangeRepository
            );
        createDummyOtcRequestMeRead(
            assetRepository,
            securityRepository,
            otcRequestRepository,
            listingRepository,
            listingHistoryRepo,
            exchangeRepository
        );
        String expectedJson =
            """
                {
                  "content": [
                    {
                      "stock": {
                        "Name": "Example One™",
                        "DividendYield": "0.05",
                        "SharesOutstanding": "325000",
                        "MarketCapitalization": null
                      },
                      "pricePerStock": {"amount": 1.00, "currency": "AUD"},
                      "premium": {"amount": 1.00, "currency": "AUD"},
                      "amount": 1,
                      "settlementDate": "2025-04-11",
                      "madeFor":"ForeignBankId[routingNumber=444, userId=a4bf370e-2129-4116-9243-0c4ead0fe43e]",
                      "latestStockPrice":{"amount":66.40,"currency":"USD"}
                    },
                    {
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
        var jwtToken = "Bearer " + JwtPlaceholders.CLIENT_TOKEN;
        mvc.get()
            .uri("/stock/otc/me")
            .param("page", "0")
            .param("size", "10")
            .header(HttpHeaders.AUTHORIZATION, jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo(expectedJson);
    }

    /**
     * Tests that the /otc/me endpoint returns an empty list when the authenticated user is not part
     * of any OTC requests.
     */
    @Test
    public void testGetMyRequestsFails() {
        OtcRequest dummyRequest =
            createDummyOtcRequestNotMe(
                assetRepository,
                securityRepository,
                otcRequestRepository,
                listingRepository,
                listingHistoryRepo,
                exchangeRepository
            );

        mvc.get()
            .uri("/stock/otc/me")
            .param("page", "0")
            .param("size", "10")
            .header("Authorization", "Bearer " + JwtPlaceholders.CLIENT_TOKEN)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .extractingPath("$.content")
            .asArray()
            .isEmpty();
    }

    /**
     * Tests that the /otc/me/unread endpoint returns only unread OTC requests for the authenticated
     * user.
     * <p>
     * The expected JSON contains the details of one unread request.
     */
    @Test
    public void testGetMyRequestsUnread() {
        OtcRequest dummyRequest =
            createDummyOtcRequestMeRead(
                assetRepository,
                securityRepository,
                otcRequestRepository,
                listingRepository,
                listingHistoryRepo,
                exchangeRepository
            );
        createDummyOtcRequestMeUnread(
            assetRepository,
            securityRepository,
            otcRequestRepository,
            listingRepository,
            listingHistoryRepo,
            exchangeRepository
        );
        String expectedJson = """
            {
              "content": [
                {
                  "stock": {
                    "Name": "Example One™",
                    "DividendYield": "0.05",
                    "SharesOutstanding": "325000",
                    "MarketCapitalization": null
                  },
                  "pricePerStock": {"amount": 1.00, "currency": "AUD"},
                  "premium": {"amount": 1.00, "currency": "AUD"},
                  "amount": 1,
                  "settlementDate": "2025-04-11",
                  "latestStockPrice":{"amount":66.40,"currency":"USD"}
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
        var jwtToken = "Bearer " + JwtPlaceholders.CLIENT_TOKEN;
        mvc.get()
            .uri("/stock/otc/me/unread")
            .param("page", "0")
            .param("size", "10")
            .header(HttpHeaders.AUTHORIZATION, jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo(expectedJson);
    }

    /**
     * Tests that the /otc/me/unread endpoint returns an empty list for unread requests when no such
     * requests exist.
     */
    @Test
    public void testGetMyRequestsUnreadFails() {
        OtcRequest dummyRequest =
            createDummyOtcRequestNotMe(
                assetRepository,
                securityRepository,
                otcRequestRepository,
                listingRepository,
                listingHistoryRepo,
                exchangeRepository
            );
        createDummyOtcRequestMeRead(
            assetRepository,
            securityRepository,
            otcRequestRepository,
            listingRepository,
            listingHistoryRepo,
            exchangeRepository
        );

        mvc.get()
            .uri("/stock/otc/me/unread")
            .param("page", "0")
            .param("size", "10")
            .header("Authorization", "Bearer " + JwtPlaceholders.CLIENT_TOKEN)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .extractingPath("$.content")
            .asArray()
            .isEmpty();
    }

    /**
     * Tests the rejection of an OTC request.
     * <p>
     * Creates a dummy OTC request not associated with the authenticated user, then performs a PATCH
     * request to the /otc/reject/{id} endpoint. It then asserts that the request's status changes
     * to REJECTED.
     */
    @Test
    public void testRejectOtc() {
        OtcRequest dummyRequest =
            createDummyOtcRequestNotMe(
                assetRepository,
                securityRepository,
                otcRequestRepository,
                listingRepository,
                listingHistoryRepo,
                exchangeRepository
            );
        var id = dummyRequest.getId();
        createDummyOtcRequestMeRead(
            assetRepository,
            securityRepository,
            otcRequestRepository,
            listingRepository,
            listingHistoryRepo,
            exchangeRepository
        );

        mvc.patch()
            .uri("/stock/otc/reject/" + id)
            .header("Authorization", "Bearer " + JwtPlaceholders.CLIENT_TOKEN)
            .assertThat()
            .hasStatusOk();
        var otcRejected =
            otcRequestRepository.findById(id)
                .get();
        assertEquals(RequestStatus.REJECTED, otcRejected.getStatus());
    }

    /**
     * Tests updating an existing OTC request.
     * <p>
     * The test updates the request with new monetary values, an updated amount, and settlement date
     * via a PATCH request. It then asserts that the corresponding fields have been updated and that
     * the modifiedBy field matches the authenticated client.
     *
     * @throws JsonProcessingException if conversion of the update DTO to JSON fails.
     */
    @Test
    public void testUpdateOtc() throws JsonProcessingException {
        OtcRequest dummyRequest =
            createDummyOtcRequestMeRead(
                assetRepository,
                securityRepository,
                otcRequestRepository,
                listingRepository,
                listingHistoryRepo,
                exchangeRepository
            );
        var id = dummyRequest.getId();
        var momo = new MonetaryAmount();
        momo.setAmount(BigDecimal.TEN);
        momo.setCurrency(CurrencyCode.CAD);
        var updateDto = new OtcRequestUpdateDto(null, momo, 10, LocalDate.parse("2025-04-11"));
        var body = objMapper.writeValueAsString(updateDto);
        mvc.patch()
            .uri("/stock/otc/update/" + id)
            .header("Authorization", "Bearer " + JwtPlaceholders.CLIENT_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(body)
            .assertThat()
            .hasStatusOk();
        var otcRejected =
            otcRequestRepository.findById(id)
                .get();
        assertEquals(
            dummyRequest.getPricePerStock()
                .getCurrency(),
            otcRejected.getPricePerStock()
                .getCurrency()
        );
        assertEquals(
            momo.getCurrency(),
            otcRejected.getPremium()
                .getCurrency()
        );
        assertEquals(10, otcRejected.getAmount());
        assertEquals(
            JwtPlaceholders.CLIENT_ID.toString(),
            otcRejected.getModifiedBy()
                .userId()
        );
    }

    /**
     * Tests creating a new OTC request.
     * <p>
     * The test sets up a dummy asset ownership record with sufficient availability, constructs an
     * OtcRequestCreateDto, and sends a POST request to the /otc/create endpoint. It then asserts
     * that the new OTC request is persisted with the expected amount, settlement date, monetary
     * fields, and correct assignment for madeBy and madeFor fields.
     *
     * @throws Exception if the POST request fails or JSON processing fails.
     */
    @Test
    public void testCreateOtcRequest() throws Exception {
        UUID callerId = JwtPlaceholders.CLIENT_ID;
        UUID assetOwnerId = UUID.fromString("1fad2c01-f82f-41a6-822c-8ca1b3232575");
        final var assetOwner = userGen.createClient(x -> x.id(assetOwnerId));

        // Set up dummy asset ownership record with sufficient available amount.
        setupDummyAssetOwnership(
            assetOwner,
            20,
            assetRepository,
            assetOwnershipRepository,
            securityRepository,
            listingRepository,
            listingHistoryRepo,
            exchangeRepository
        );
        System.out.println(
            assetOwnershipRepository.findByMyId(assetOwnerId, AssetGenerator.STOCK_EX1_UUID)
        );
        System.out.println(assetOwnerId + "  " + AssetGenerator.STOCK_EX1_UUID);
        for (AssetOwnership assetOwnership : assetOwnershipRepository.findAll()) {
            System.out.println(
                assetOwnership.getId()
                    .getAsset()
                    .getId()
                    + " "
                    + assetOwnership.getId()
                        .getUser()
            );
        }
        MonetaryAmount price = new MonetaryAmount();
        price.setAmount(BigDecimal.valueOf(150.00));
        price.setCurrency(CurrencyCode.AUD);

        MonetaryAmount premium = new MonetaryAmount();
        premium.setAmount(BigDecimal.valueOf(400.00));
        premium.setCurrency(CurrencyCode.AUD);

        int amount = 10;
        LocalDate settlementDate = LocalDate.parse("2025-05-22");

        OtcRequestCreateDto createDto =
            new OtcRequestCreateDto(
                assetOwnerId,
                AssetGenerator.STOCK_EX1_UUID,
                price,
                premium,
                amount,
                settlementDate
            );

        String jsonBody = objMapper.writeValueAsString(createDto);

        mvc.post()
            .uri("/stock/otc/create")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + JwtPlaceholders.CLIENT_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonBody)
            .assertThat()
            .hasStatusOk();

        List<OtcRequest> otcRequests = otcRequestRepository.findAll();
        assertFalse(otcRequests.isEmpty(), "No OTC request found after creation");

        OtcRequest createdRequest = otcRequests.get(otcRequests.size() - 1);

        assertEquals(amount, createdRequest.getAmount(), "Amount mismatch");
        assertEquals(
            settlementDate,
            createdRequest.getSettlementDate(),
            "Settlement date mismatch"
        );

        assertEquals(
            price.getCurrency(),
            createdRequest.getPricePerStock()
                .getCurrency(),
            "Price currency mismatch"
        );
        assertEquals(
            premium.getCurrency(),
            createdRequest.getPremium()
                .getCurrency(),
            "Premium currency mismatch"
        );

        ForeignBankId expectedMadeBy =
            new ForeignBankId(BankRoutingNumber.BANK4.getRoutingNumber(), callerId.toString());
        assertEquals(
            expectedMadeBy.userId(),
            createdRequest.getMadeBy()
                .userId(),
            "madeBy userId mismatch"
        );

        ForeignBankId expectedMadeFor =
            new ForeignBankId(BankRoutingNumber.BANK4.getRoutingNumber(), assetOwnerId.toString());
        assertEquals(
            expectedMadeFor.userId(),
            createdRequest.getMadeFor()
                .userId(),
            "madeFor userId mismatch"
        );
    }

    /**
     * Tests the expiration of finished OTC requests.
     *
     * <p>
     * This test creates an OTC request in the FINISHED state with a settlement date that has
     * passed, as well as an asset ownership record with reserved and public amounts. It then
     * invokes the cron job method, asserting that the OTC request’s status is updated to EXPIRED
     * and that the asset ownership record has its reserved amount decreased and its public amount
     * increased accordingly.
     */
    @Autowired
    private OtcRequestExpiryService expiryService;

    @Test
    @Transactional
    public void testExpireFinishedOtcRequests() {
        final var assetOwner = userGen.createClient(x -> x.id(JwtPlaceholders.CLIENT_ID));
        int initialReserved = 10;
        int initialPublic = 5;

        int requestAmount = 5;
        OtcRequest request =
            createDummyOtcRequestMeFinished(
                assetRepository,
                securityRepository,
                otcRequestRepository,
                requestAmount,
                listingRepository,
                listingHistoryRepo,
                exchangeRepository
            );
        var assetOwn =
            createDummyAssetOwnership(
                assetOwner,
                initialReserved,
                initialPublic,
                assetRepository,
                assetOwnershipRepository
            );

        expiryService.expireFinishedOtcRequests();

        var updatedRequestOpt = otcRequestRepository.findById(request.getId());
        assertTrue(updatedRequestOpt.isPresent(), "Expected OTC request to be present");
        OtcRequest updatedRequest = updatedRequestOpt.get();
        assertEquals(
            RequestStatus.EXPIRED,
            updatedRequest.getStatus(),
            "OTC request status should be EXPIRED"
        );
        assetOwnershipRepository.findAll()
            .forEach(System.out::println);
        var assetOwnershipOpt = assetOwnershipRepository.findById(assetOwn.getId());
        assertTrue(assetOwnershipOpt.isPresent(), "Expected asset ownership record to be present");
        AssetOwnership assetOwnership = assetOwnershipOpt.get();

        int expectedReserved = initialReserved - requestAmount;
        int expectedPublic = initialPublic + requestAmount;
        assertEquals(
            expectedReserved,
            assetOwnership.getReservedAmount(),
            "Reserved amount mismatch"
        );
        assertEquals(expectedPublic, assetOwnership.getPublicAmount(), "Public amount mismatch");
    }
}
