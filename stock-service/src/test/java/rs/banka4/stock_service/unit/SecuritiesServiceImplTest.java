package rs.banka4.stock_service.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import rs.banka4.rafeisen.common.currency.CurrencyCode;
import rs.banka4.rafeisen.common.security.AuthenticatedBankUserAuthentication;
import rs.banka4.stock_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.stock_service.domain.listing.dtos.ListingDetailsDto;
import rs.banka4.stock_service.domain.orders.db.Direction;
import rs.banka4.stock_service.domain.orders.db.Order;
import rs.banka4.stock_service.domain.orders.db.Status;
import rs.banka4.stock_service.domain.security.Security;
import rs.banka4.stock_service.domain.security.forex.db.ForexPair;
import rs.banka4.stock_service.domain.security.future.db.Future;
import rs.banka4.stock_service.domain.security.responses.SecurityOwnershipResponse;
import rs.banka4.stock_service.domain.security.responses.TaxSummaryResponse;
import rs.banka4.stock_service.domain.security.responses.TotalProfitResponse;
import rs.banka4.stock_service.domain.security.stock.db.Stock;
import rs.banka4.stock_service.repositories.OrderRepository;
import rs.banka4.stock_service.service.abstraction.ListingService;
import rs.banka4.stock_service.service.impl.SecuritiesServiceImpl;
import rs.banka4.testlib.integration.DbEnabledTest;

@SpringBootTest
@DbEnabledTest
@ExtendWith(MockitoExtension.class)
public class SecuritiesServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ListingService listingService;

    @InjectMocks
    private SecuritiesServiceImpl service;

    private final UUID userId = UUID.randomUUID();

    // The ticker must be a valid UUID string since getCurrentPrice parses it as such.
    private final String stockTicker =
        UUID.randomUUID()
            .toString();
    private final Security stock =
        Stock.builder()
            .id(UUID.randomUUID())
            .ticker(stockTicker)
            .build();

    private final Security forex =
        ForexPair.builder()
            .id(UUID.randomUUID())
            .ticker(
                UUID.randomUUID()
                    .toString()
            ) // Not used in getCurrentPrice.
            .exchangeRate(new BigDecimal("1.18"))
            .build();

    /**
     * Verify that when no orders are present, getMySecurities returns an empty list.
     */
    @Test
    public void getMySecurities_shouldReturnEmptyListWhenNoOrders() {
        // Given
        when(orderRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        Authentication auth = createAuthentication(userId);

        // When
        List<SecurityOwnershipResponse> result = service.getMySecurities(auth);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Verify that a stock security order is mapped correctly.
     */
    @Test
    public void getMySecurities_shouldMapStockSecurityCorrectly() {
        // Given
        Order order = createOrder(stock, 100, "150.00", Direction.BUY);
        Authentication auth = createAuthentication(userId);

        when(orderRepository.findByUserId(userId)).thenReturn(List.of(order));
        when(listingService.getListingDetails(UUID.fromString(stock.getTicker()))).thenReturn(
            new TestListingDetails(new BigDecimal("172.50"))
        );
        when(
            orderRepository.findByUserIdAndAssetAndDirectionAndIsDone(
                userId,
                stock,
                Direction.BUY,
                true
            )
        ).thenReturn(List.of(order));

        // When
        List<SecurityOwnershipResponse> result = service.getMySecurities(auth);

        // Then
        assertThat(result).hasSize(1);
        SecurityOwnershipResponse response = result.get(0);
        // The enum's string value should be "Stock"
        assertThat(response.type()).isEqualTo("Stock");
        assertThat(response.ticker()).isEqualTo(stock.getTicker());
        assertThat(response.amount()).isEqualTo(100);
        assertThat(response.price()).isEqualTo(new BigDecimal("172.50"));
        // Profit calculation: (172.50 - 150.00) * 100 = 2250.00
        assertThat(response.profit()).isEqualTo(new BigDecimal("2250.00"));
    }

    /**
     * Verify that the service calculates the price for a ForexPair from its exchange rate.
     */
    @Test
    public void getMySecurities_shouldCalculateForexPriceFromExchangeRate() {
        // Given
        Order order = createOrder(forex, 5000, "1.15", Direction.BUY);
        Authentication auth = createAuthentication(userId);

        when(orderRepository.findByUserId(userId)).thenReturn(List.of(order));
        when(
            orderRepository.findByUserIdAndAssetAndDirectionAndIsDone(
                userId,
                forex,
                Direction.BUY,
                true
            )
        ).thenReturn(List.of(order));

        // When
        List<SecurityOwnershipResponse> result = service.getMySecurities(auth);

        // Then For Forex, getCurrentPrice returns the exchange rate.
        assertThat(
            result.get(0)
                .price()
        ).isEqualTo(new BigDecimal("1.18"));
    }

    /**
     * Verify that when multiple buy orders exist along with a sell order, the service calculates
     * the net holding amount and profit correctly.
     */
    @Test
    public void getMySecurities_shouldCalculateAverageCostFromMultipleBuys() {
        // Given
        Order buy1 = createOrder(stock, 50, "140.00", Direction.BUY);
        Order buy2 = createOrder(stock, 30, "160.00", Direction.BUY);
        Order sell = createOrder(stock, 20, "170.00", Direction.SELL);
        Authentication auth = createAuthentication(userId);

        // Assume overall holding aggregates to a net quantity of 60 (50 + 30 - 20)
        when(orderRepository.findByUserId(userId)).thenReturn(List.of(buy1, buy2, sell));
        when(listingService.getListingDetails(UUID.fromString(stock.getTicker()))).thenReturn(
            new TestListingDetails(new BigDecimal("180.00"))
        );
        // Only BUY orders are used for profit calculation.
        when(
            orderRepository.findByUserIdAndAssetAndDirectionAndIsDone(
                userId,
                stock,
                Direction.BUY,
                true
            )
        ).thenReturn(List.of(buy1, buy2));

        // When
        List<SecurityOwnershipResponse> result = service.getMySecurities(auth);

        // Then
        SecurityOwnershipResponse response = result.get(0);
        // Expected net amount: 50 + 30 - 20 = 60
        assertThat(response.amount()).isEqualTo(60);
        // Calculations: Total buy cost = (50 * 140.00) + (30 * 160.00) = 7000 + 4800 = 11800 Total
        // quantity bought = 80, average cost = 11800 / 80 = 147.50 Profit = (180.00 - 147.50) * 60
        // = 32.50 * 60 = 1950.00
        assertThat(response.profit()).isEqualTo(new BigDecimal("1950.00"));
    }

    /**
     * Verify that when no BUY orders exist, the profit is zero.
     */
    @Test
    public void getMySecurities_shouldReturnZeroProfitWhenNoBuyOrders() {
        // Given
        Order sellOrder = createOrder(stock, 20, "170.00", Direction.SELL);
        Authentication auth = createAuthentication(userId);

        when(orderRepository.findByUserId(userId)).thenReturn(List.of(sellOrder));
        when(listingService.getListingDetails(UUID.fromString(stock.getTicker()))).thenReturn(
            new TestListingDetails(new BigDecimal("170.00"))
        );
        when(
            orderRepository.findByUserIdAndAssetAndDirectionAndIsDone(
                userId,
                stock,
                Direction.BUY,
                true
            )
        ).thenReturn(Collections.emptyList());

        // When
        List<SecurityOwnershipResponse> result = service.getMySecurities(auth);

        // Then
        assertThat(
            result.get(0)
                .profit()
        ).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void getTotalStockProfit_shouldSumStockProfitsOnly() {
        // Setup different security types
        Authentication auth = createAuthentication(userId);

        Security stock1 = createStock("AAPL", new BigDecimal("200.00"));
        Security stock2 = createStock("GOOGL", new BigDecimal("1500.00"));
        Security future = createFuture("MESA", new BigDecimal("11200.00"));
        Security forex = createForexPair("FAT", new BigDecimal("1337.00"));

        List<Order> orders =
            List.of(
                createOrder(stock1, 10, "180.00", Direction.BUY),
                createOrder(stock2, 2, "1400.00", Direction.BUY),
                createOrder(future, 5, "20.00", Direction.BUY),
                createOrder(forex, 1000, "1.10", Direction.BUY)
            );

        when(orderRepository.findByUserId(userId)).thenReturn(orders);
        when(listingService.getListingDetails(any())).thenAnswer(inv -> {
            UUID securityId = inv.getArgument(0);
            return new TestListingDetails(
                securityId.equals(stock1.getId()) ? new BigDecimal("200.00")
                    : securityId.equals(stock2.getId()) ? new BigDecimal("1500.00")
                    : new BigDecimal("0.00")
            );
        });

        // When
        TotalProfitResponse response =
            service.getTotalUnrealizedProfit(auth)
                .getBody();

        // Then
        assertThat(response.totalProfit()).isEqualTo(new BigDecimal("540.00")); // (200-180)*10 +
                                                                                // (1500-1400)*2
        assertThat(response.currency()).isEqualTo("USD");
    }

    /**
     * Test that when no orders exist the tax summary returns zero for both paid and unpaid tax.
     */
    @Test
    public void testGetTaxSummary_noOrders() {
        Authentication auth = createAuthentication(userId);
        when(orderRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        ResponseEntity<TaxSummaryResponse> response = service.getTaxSummary(auth);
        TaxSummaryResponse summary = response.getBody();

        assertNotNull(summary);
        // Expect both tax amounts to be zero
        assertEquals(BigDecimal.ZERO, summary.paidTaxThisYear());
        assertEquals(BigDecimal.ZERO, summary.unpaidTaxThisMonth());
        assertEquals("RSD", summary.currency());
    }

    /**
     * Test that a sell order created in the current month calculates unpaid tax. Scenario: - A BUY
     * order: quantity 100 @ 100.00 => average cost 100.00 - A SELL order: quantity 50 @ 150.00 =>
     * gain per unit = 50; total gain = 50 * 50 = 2500; Tax = 15% of 2500 = 375.00 The SELL order is
     * dated in the current month so the tax is treated as "unpaid".
     */
    @Test
    public void testGetTaxSummary_sellOrder_currentMonth() {
        Authentication auth = createAuthentication(userId);
        Stock stock =
            Stock.builder()
                .id(UUID.randomUUID())
                .ticker("TEST")
                .name("Test Stock")
                .build();

        OffsetDateTime now = OffsetDateTime.now();
        // Create a BUY order (completed)
        Order buyOrder =
            Order.builder()
                .userId(userId)
                .asset(stock)
                .quantity(100)
                .pricePerUnit(new MonetaryAmount(new BigDecimal("100.00"), CurrencyCode.USD))
                .direction(Direction.BUY)
                .status(Status.APPROVED)
                .isDone(true)
                .createdAt(now.minusDays(10)) // earlier in current month/year
                .lastModified(now.minusDays(10))
                .contractSize(1)
                .remainingPortions(100)
                .afterHours(false)
                .accountId(UUID.randomUUID())
                .used(false)
                .build();

        // Create a SELL order (completed) in the current month
        Order sellOrder =
            Order.builder()
                .userId(userId)
                .asset(stock)
                .quantity(50)
                .pricePerUnit(new MonetaryAmount(new BigDecimal("150.00"), CurrencyCode.USD))
                .direction(Direction.SELL)
                .status(Status.APPROVED)
                .isDone(true)
                .createdAt(now) // current month
                .lastModified(now)
                .contractSize(1)
                .remainingPortions(50)
                .afterHours(false)
                .accountId(UUID.randomUUID())
                .used(false)
                .build();

        // The orderRepository.findByUserId should return both orders, though only the SELL order is
        // processed.
        when(orderRepository.findByUserId(userId)).thenReturn(List.of(buyOrder, sellOrder));
        // When fetching BUY orders for tax computation, return the buy order.
        when(
            orderRepository.findByUserIdAndAssetAndDirectionAndIsDone(
                eq(userId),
                eq(stock),
                eq(Direction.BUY),
                eq(true)
            )
        ).thenReturn(List.of(buyOrder));

        ResponseEntity<TaxSummaryResponse> response = service.getTaxSummary(auth);
        TaxSummaryResponse summary = response.getBody();

        assertNotNull(summary);
        // As computed: tax = (150 - 100)*50*0.15 = 375.00. Since sell order is in current month:
        assertEquals(new BigDecimal("0.00"), summary.paidTaxThisYear());
        assertEquals(new BigDecimal("375.00"), summary.unpaidTaxThisMonth());
        assertEquals("RSD", summary.currency());
    }

    /**
     * Test that a sell order created in a previous month in the current year results in paid tax.
     * The tax amount is computed similarly to the previous test, but since the order's createdAt is
     * from last month, its tax is recorded as "paidTaxThisYear".
     */
    @Test
    public void testGetTaxSummary_sellOrder_previousMonth() {
        Authentication auth = createAuthentication(userId);
        Stock stock =
            Stock.builder()
                .id(UUID.randomUUID())
                .ticker("TEST")
                .name("Test Stock")
                .build();

        OffsetDateTime now = OffsetDateTime.now();
        // Create a BUY order
        Order buyOrder =
            Order.builder()
                .userId(userId)
                .asset(stock)
                .quantity(100)
                .pricePerUnit(new MonetaryAmount(new BigDecimal("100.00"), CurrencyCode.USD))
                .direction(Direction.BUY)
                .status(Status.APPROVED)
                .isDone(true)
                .createdAt(now.minusDays(20))
                .lastModified(now.minusDays(20))
                .contractSize(1)
                .remainingPortions(100)
                .afterHours(false)
                .accountId(UUID.randomUUID())
                .used(false)
                .build();

        // Create a SELL order from a previous month
        OffsetDateTime previousMonthDate = now.minusMonths(1);
        Order sellOrder =
            Order.builder()
                .userId(userId)
                .asset(stock)
                .quantity(50)
                .pricePerUnit(new MonetaryAmount(new BigDecimal("150.00"), CurrencyCode.USD))
                .direction(Direction.SELL)
                .status(Status.APPROVED)
                .isDone(true)
                .createdAt(previousMonthDate)
                .lastModified(previousMonthDate)
                .contractSize(1)
                .remainingPortions(50)
                .afterHours(false)
                .accountId(UUID.randomUUID())
                .used(false)
                .build();

        when(orderRepository.findByUserId(userId)).thenReturn(List.of(buyOrder, sellOrder));
        when(
            orderRepository.findByUserIdAndAssetAndDirectionAndIsDone(
                eq(userId),
                eq(stock),
                eq(Direction.BUY),
                eq(true)
            )
        ).thenReturn(List.of(buyOrder));

        ResponseEntity<TaxSummaryResponse> response = service.getTaxSummary(auth);
        TaxSummaryResponse summary = response.getBody();

        assertNotNull(summary);
        // Expected tax is the same: 375.00, but since sell order is not in the current month it
        // counts as paid tax.
        assertEquals(new BigDecimal("375.00"), summary.paidTaxThisYear());
        assertEquals(new BigDecimal("0.00"), summary.unpaidTaxThisMonth());
        assertEquals("RSD", summary.currency());
    }

    /**
     * Test that orders for non-stock assets (e.g. a Future) are ignored.
     */
    @Test
    public void testGetTaxSummary_ignoreNonStockOrder() {
        Authentication auth = createAuthentication(userId);
        // Create a non-stock asset (Future in this example)
        Future futureAsset =
            Future.builder()
                .id(UUID.randomUUID())
                .ticker("FUT123")
                .name("Test Future")
                .build();

        OffsetDateTime now = OffsetDateTime.now();
        Order sellOrder =
            Order.builder()
                .userId(userId)
                .asset(futureAsset)
                .quantity(50)
                .pricePerUnit(new MonetaryAmount(new BigDecimal("150.00"), CurrencyCode.USD))
                .direction(Direction.SELL)
                .status(Status.APPROVED)
                .isDone(true)
                .createdAt(now)
                .lastModified(now)
                .contractSize(1)
                .remainingPortions(50)
                .afterHours(false)
                .accountId(UUID.randomUUID())
                .used(false)
                .build();

        when(orderRepository.findByUserId(userId)).thenReturn(List.of(sellOrder));
        // No BUY orders for non-stock asset
        when(
            orderRepository.findByUserIdAndAssetAndDirectionAndIsDone(
                eq(userId),
                eq(futureAsset),
                eq(Direction.BUY),
                eq(true)
            )
        ).thenReturn(Collections.emptyList());

        ResponseEntity<TaxSummaryResponse> response = service.getTaxSummary(auth);
        TaxSummaryResponse summary = response.getBody();

        assertNotNull(summary);
        // Since the asset is not a stock, no tax should be computed.
        assertEquals(BigDecimal.ZERO, summary.paidTaxThisYear());
        assertEquals(BigDecimal.ZERO, summary.unpaidTaxThisMonth());
        assertEquals("RSD", summary.currency());
    }


    private Security createFuture(String ticker, BigDecimal contractSize) {
        return Future.builder()
            .id(UUID.randomUUID())
            .ticker(ticker)
            .contractSize(
                contractSize.toBigInteger()
                    .longValue()
            )
            .build();
    }

    private Security createForexPair(String ticker, BigDecimal price) {
        return ForexPair.builder()
            .id(UUID.randomUUID())
            .ticker(ticker)
            .exchangeRate(price)
            .build();
    }

    private Security createStock(String ticker, BigDecimal price) {
        return Stock.builder()
            .id(UUID.randomUUID())
            .ticker(ticker)
            .build();
    }

    /**
     * Creates an Order for testing.
     *
     * @param security the asset for the order
     * @param quantity the amount of the asset
     * @param price the price per unit as a string
     * @param direction the order direction (BUY/SELL)
     * @return a new Order instance
     */
    private Order createOrder(Security security, int quantity, String price, Direction direction) {
        return Order.builder()
            .userId(userId)
            .asset(security)
            .quantity(quantity)
            .pricePerUnit(new MonetaryAmount(new BigDecimal(price), CurrencyCode.USD))
            .direction(direction)
            .isDone(true)
            .lastModified(OffsetDateTime.now())
            .build();
    }

    /**
     * Creates a mocked Authentication object with a principal that returns the specified userId.
     *
     * @param userId the user identifier to be returned by the principal
     * @return a mocked instance of AuthenticatedBankUserAuthentication
     */
    private Authentication createAuthentication(UUID userId) {
        AuthenticatedBankUserAuthentication auth =
            Mockito.mock(AuthenticatedBankUserAuthentication.class);
        when(
            auth.getPrincipal()
                .userId()
        ).thenReturn(userId);
        return auth;
    }


    /**
     * Private static inner class to simulate the response from
     * listingService.getListingDetails(UUID).
     */
    private static class TestListingDetails extends ListingDetailsDto {
        private final BigDecimal price;

        TestListingDetails(BigDecimal price) {
            this.price = price;
        }

        public BigDecimal getPrice() {
            return price;
        }
    }
}
