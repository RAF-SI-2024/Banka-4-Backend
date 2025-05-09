package rs.banka4.bank_service.generator;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import rs.banka4.bank_service.domain.account.db.Account;
import rs.banka4.bank_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.bank_service.domain.options.db.Asset;
import rs.banka4.bank_service.domain.orders.db.Direction;
import rs.banka4.bank_service.domain.orders.db.Order;
import rs.banka4.bank_service.domain.orders.db.OrderType;
import rs.banka4.bank_service.domain.orders.db.Status;
import rs.banka4.bank_service.domain.orders.dtos.CreateOrderDto;
import rs.banka4.bank_service.domain.orders.dtos.OrderDto;
import rs.banka4.bank_service.domain.user.User;
import rs.banka4.rafeisen.common.currency.CurrencyCode;

public class OrderObjectMother {

    /**
     * Generates a basic Approved Market BUY order.
     *
     * @return a new Order instance representing a basic BUY order
     */
    public static Order generateBasicOrder(User user, Account account) {
        Asset asset = AssetObjectMother.generateBasicStock();

        return Order.builder()
            .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
            .user(user)
            .asset(asset)
            .orderType(OrderType.MARKET)
            .quantity(100)
            .contractSize(1)
            .pricePerUnit(new MonetaryAmount(BigDecimal.valueOf(150.00), CurrencyCode.RSD))
            .direction(Direction.BUY)
            .status(Status.APPROVED)
            .approvedBy(null)
            .isDone(false)
            .lastModified(OffsetDateTime.now())
            .createdAt(OffsetDateTime.now())
            .remainingPortions(100)
            .afterHours(false)
            .limitValue(null)
            .stopValue(null)
            .allOrNothing(false)
            .margin(false)
            .account(account)
            .used(false)
            .build();
    }

    /**
     * Generates a basic CreateOrderDto instance with the specified direction.
     *
     * @param direction the direction of the order (e.g., BUY, SELL)
     * @return a new CreateOrderDto instance
     */
    public static CreateOrderDto generateBasicCreateOrderDto(Direction direction) {
        return new CreateOrderDto(
            UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
            direction,
            100,
            null,
            null,
            false,
            false,
            "4440001000000000220"
        );
    }

    /**
     * Generates a basic CreateOrderDto instance for a limit order.
     *
     * @return a new CreateOrderDto instance for a limit order
     */
    public static CreateOrderDto generateBasicCreateLimitOrderDto() {
        return new CreateOrderDto(
            UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
            Direction.BUY,
            100,
            new MonetaryAmount(BigDecimal.valueOf(1000.0), CurrencyCode.RSD),
            null,
            false,
            false,
            "4440001000000000220"
        );
    }

    /**
     * Generates a basic CreateOrderDto instance for a stop order.
     *
     * @return a new CreateOrderDto instance for a stop order
     */
    public static CreateOrderDto generateBasicCreateStopOrderDto() {
        return new CreateOrderDto(
            UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
            Direction.BUY,
            100,
            null,
            new MonetaryAmount(BigDecimal.valueOf(1000.0), CurrencyCode.RSD),
            false,
            false,
            "4440001000000000220"
        );
    }

    /**
     * Generates a basic OrderDto instance with the specified order type.
     *
     * @param orderType the type of the order (e.g., MARKET, LIMIT, STOP)
     * @return a new OrderDto instance
     */
    public static OrderDto generateBasicOrderDto(OrderType orderType) {
        return new OrderDto(
            UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
            "Mehmedalija",
            "Mehmedic",
            "TEST_ASSET",
            orderType,
            Direction.BUY,
            100,
            new MonetaryAmount(BigDecimal.valueOf(150.00), CurrencyCode.RSD),
            Status.APPROVED,
            false,
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            2,
            100
        );
    }

}
