package rs.banka4.stock_service.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.sun.security.auth.UserPrincipal;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import rs.banka4.rafeisen.common.security.AuthenticatedBankUserAuthentication;
import rs.banka4.stock_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.stock_service.domain.listing.dtos.ListingDetailsDto;
import rs.banka4.stock_service.domain.orders.db.Direction;
import rs.banka4.stock_service.domain.orders.db.Order;
import rs.banka4.stock_service.domain.security.Security;
import rs.banka4.stock_service.domain.security.forex.db.CurrencyCode;
import rs.banka4.stock_service.domain.security.forex.db.ForexPair;
import rs.banka4.stock_service.domain.security.responses.SecurityOwnershipResponse;
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
    private final String stockTicker = UUID.randomUUID().toString();
    private final Security stock = Stock.builder()
        .id(UUID.randomUUID())
        .ticker(stockTicker)
        .build();

    private final Security forex = ForexPair.builder()
        .id(UUID.randomUUID())
        .ticker(UUID.randomUUID().toString()) // Not used in getCurrentPrice.
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
        when(listingService.getListingDetails(UUID.fromString(stock.getTicker())))
            .thenReturn(new TestListingDetails(new BigDecimal("172.50")));
        when(orderRepository.findByUserIdAndAssetAndDirectionAndIsDone(
            userId,
            stock,
            Direction.BUY,
            true))
            .thenReturn(List.of(order));

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
        when(orderRepository.findByUserIdAndAssetAndDirectionAndIsDone(
            userId,
            forex,
            Direction.BUY,
            true))
            .thenReturn(List.of(order));

        // When
        List<SecurityOwnershipResponse> result = service.getMySecurities(auth);

        // Then
        // For Forex, getCurrentPrice returns the exchange rate.
        assertThat(result.get(0).price()).isEqualTo(new BigDecimal("1.18"));
    }

    /**
     * Verify that when multiple buy orders exist along with a sell order,
     * the service calculates the net holding amount and profit correctly.
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
        when(listingService.getListingDetails(UUID.fromString(stock.getTicker())))
            .thenReturn(new TestListingDetails(new BigDecimal("180.00")));
        // Only BUY orders are used for profit calculation.
        when(orderRepository.findByUserIdAndAssetAndDirectionAndIsDone(
            userId,
            stock,
            Direction.BUY,
            true))
            .thenReturn(List.of(buy1, buy2));

        // When
        List<SecurityOwnershipResponse> result = service.getMySecurities(auth);

        // Then
        SecurityOwnershipResponse response = result.get(0);
        // Expected net amount: 50 + 30 - 20 = 60
        assertThat(response.amount()).isEqualTo(60);
        // Calculations:
        // Total buy cost = (50 * 140.00) + (30 * 160.00) = 7000 + 4800 = 11800
        // Total quantity bought = 80, average cost = 11800 / 80 = 147.50
        // Profit = (180.00 - 147.50) * 60 = 32.50 * 60 = 1950.00
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
        when(listingService.getListingDetails(UUID.fromString(stock.getTicker())))
            .thenReturn(new TestListingDetails(new BigDecimal("170.00")));
        when(orderRepository.findByUserIdAndAssetAndDirectionAndIsDone(
            userId,
            stock,
            Direction.BUY,
            true))
            .thenReturn(Collections.emptyList());

        // When
        List<SecurityOwnershipResponse> result = service.getMySecurities(auth);

        // Then
        assertThat(result.get(0).profit()).isEqualTo(BigDecimal.ZERO);
    }

    /**
     * Creates an Order for testing.
     *
     * @param security  the asset for the order
     * @param quantity  the amount of the asset
     * @param price     the price per unit as a string
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
        AuthenticatedBankUserAuthentication auth = Mockito.mock(AuthenticatedBankUserAuthentication.class);
        when(auth.getPrincipal().userId()).thenReturn(userId);
        return auth;
    }


    /**
     * Private static inner class to simulate the response from listingService.getListingDetails(UUID).
     */
    private static class TestListingDetails extends ListingDetailsDto{
        private final BigDecimal price;

        TestListingDetails(BigDecimal price) {
            this.price = price;
        }

        public BigDecimal getPrice() {
            return price;
        }
    }
}
