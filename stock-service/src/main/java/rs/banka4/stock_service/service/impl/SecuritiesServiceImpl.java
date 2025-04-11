package rs.banka4.stock_service.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import rs.banka4.rafeisen.common.security.AuthenticatedBankUserAuthentication;
import rs.banka4.stock_service.domain.options.db.Asset;
import rs.banka4.stock_service.domain.orders.db.Direction;
import rs.banka4.stock_service.domain.orders.db.Order;
import rs.banka4.stock_service.domain.security.Security;
import rs.banka4.stock_service.domain.security.SecurityDto;
import rs.banka4.stock_service.domain.security.forex.db.ForexPair;
import rs.banka4.stock_service.domain.security.future.db.Future;
import rs.banka4.stock_service.domain.security.responses.SecurityOwnershipResponse;
import rs.banka4.stock_service.domain.security.responses.SecurityTypeDto;
import rs.banka4.stock_service.domain.security.stock.db.Stock;
import rs.banka4.stock_service.repositories.OrderRepository;
import rs.banka4.stock_service.service.abstraction.ListingService;
import rs.banka4.stock_service.service.abstraction.SecuritiesService;

@Service
@RequiredArgsConstructor
public class SecuritiesServiceImpl implements SecuritiesService {
    private final OrderRepository orderRepository;
    private final ListingService listingService;

    @Override
    public ResponseEntity<Page<SecurityDto>> getSecurities(
        String securityType,
        String name,
        Pageable pageable
    ) {
        return null;
    }

    @Override
    public List<SecurityOwnershipResponse> getMySecurities(Authentication authentication) {
        UUID userId = getCurrentUserId(authentication);
        List<Order> holdings = orderRepository.findByUserId(userId);

        return holdings.stream()
            .map(order -> mapToOrderOwnershipResponse(order, userId))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Maps an {@link Order} to a {@link SecurityOwnershipResponse} for a given user.
     *
     * @param order  the order to map from
     * @param userId the current user's unique identifier
     * @return a SecurityOwnershipResponse object containing details of the security holding;
     *         returns {@code null} if the asset is not an instance of {@link Security}
     */
    private SecurityOwnershipResponse mapToOrderOwnershipResponse(Order order, UUID userId) {
        if (order.getAsset() instanceof Security security) {
            int amount = order.getQuantity();
            BigDecimal currentPrice = getCurrentPrice(security);
            BigDecimal profit = calculateProfit(userId, security, amount, currentPrice);

            return new SecurityOwnershipResponse(
                determineAssetType(security),
                security.getTicker(),
                amount,
                currentPrice,
                profit,
                getLastModified(security, userId)
            );
        }
        return null;
    }

    /**
     * Determines the security type of the provided asset.
     *
     * @param asset the asset whose type is to be determined
     * @return the {@link SecurityTypeDto} that represents the asset type
     * @throws IllegalArgumentException if the asset type is not supported
     */
    private SecurityTypeDto determineAssetType(Asset asset) {
        if (asset instanceof Stock) return SecurityTypeDto.STOCK;
        if (asset instanceof Future) return SecurityTypeDto.FUTURE;
        if (asset instanceof ForexPair) return SecurityTypeDto.FOREX;
        throw new IllegalArgumentException("Unsupported asset type");
    }

    /**
     * Retrieves the current price of a given security.
     *
     * <p>
     * For securities that are instances of {@link ForexPair}, the current price is its exchange rate.
     * For other securities, it fetches the latest price from the {@link ListingService}.
     * </p>
     *
     * @param security the security for which the current price is retrieved
     * @return the current price as a {@link BigDecimal}
     */
    private BigDecimal getCurrentPrice(Security security) {
        if (security instanceof ForexPair) {
            return ((ForexPair) security).getExchangeRate();
        }
        return listingService.getListingDetails(security.getId())
            .getPrice();
    }

    /**
     * Calculates the profit for a user on a given security holding.
     *
     * <p>
     * This method computes the total cost of buy orders for the security, calculates the average cost,
     * and then determines the difference between the current price and the average cost scaled by the order amount.
     * </p>
     *
     * @param userId       the unique identifier of the user
     * @param security     the security for which the profit is calculated
     * @param amount       the quantity of the security held
     * @param currentPrice the latest price of the security
     * @return the calculated profit as a {@link BigDecimal}; returns zero if no buy orders exist
     */
    private BigDecimal calculateProfit(UUID userId, Security security, int amount, BigDecimal currentPrice) {
        List<Order> buyOrders =
            orderRepository.findByUserIdAndAssetAndDirectionAndIsDone(
                userId,
                security,
                Direction.BUY,
                true
            );

        BigDecimal totalBuyCost = BigDecimal.ZERO;
        BigDecimal totalBuyQuantity = BigDecimal.ZERO;

        for (Order order : buyOrders) {
            BigDecimal quantity = BigDecimal.valueOf(order.getQuantity());
            BigDecimal price = order.getPricePerUnit().getAmount();
            totalBuyCost = totalBuyCost.add(price.multiply(quantity));
            totalBuyQuantity = totalBuyQuantity.add(quantity);
        }

        if (totalBuyQuantity.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal averageCost = totalBuyCost.divide(totalBuyQuantity, RoundingMode.HALF_UP);
        return currentPrice.subtract(averageCost)
            .multiply(BigDecimal.valueOf(amount));
    }

    /**
     * Retrieves the last modified date of the most recent completed buy order for the specified security.
     *
     * @param security the security for which the last modification time is desired
     * @param userId   the unique identifier of the user
     * @return the last modified {@link OffsetDateTime} of the newest buy order;
     *         returns {@code null} if no such order exists
     */
    private OffsetDateTime getLastModified(Security security, UUID userId) {
        Order newestOrder =
            orderRepository.findNewestOrder(userId, security, Direction.BUY, true);
        return newestOrder != null ? newestOrder.getLastModified() : null;
    }

    /**
     * Extracts the current user's identifier from the provided authentication details.
     *
     * @param authentication the authentication object containing user credentials
     * @return the user's UUID as obtained from the authentication principal
     */
    private UUID getCurrentUserId(Authentication authentication) {
        final var ourAuth = (AuthenticatedBankUserAuthentication) authentication;
        return ourAuth.getPrincipal().userId();
    }

    /**
     * Calculates the total quantity from a list of orders.
     *
     * @param orders the list of orders for which the quantity is summed
     * @return the total sum of quantities in the provided orders
     */
    private long totalQuantity(List<Order> orders) {
        return orders.stream()
            .mapToLong(Order::getQuantity)
            .sum();
    }
}
