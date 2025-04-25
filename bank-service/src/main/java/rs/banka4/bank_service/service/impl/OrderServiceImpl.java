package rs.banka4.bank_service.service.impl;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import rs.banka4.bank_service.domain.account.db.Account;
import rs.banka4.bank_service.domain.actuaries.db.ActuaryInfo;
import rs.banka4.bank_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.bank_service.domain.exchanges.db.Exchange;
import rs.banka4.bank_service.domain.listing.db.Listing;
import rs.banka4.bank_service.domain.options.db.Asset;
import rs.banka4.bank_service.domain.options.db.Option;
import rs.banka4.bank_service.domain.orders.db.Direction;
import rs.banka4.bank_service.domain.orders.db.Order;
import rs.banka4.bank_service.domain.orders.db.OrderType;
import rs.banka4.bank_service.domain.orders.db.Status;
import rs.banka4.bank_service.domain.orders.dtos.*;
import rs.banka4.bank_service.domain.orders.mapper.OrderMapper;
import rs.banka4.bank_service.domain.security.future.db.Future;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;
import rs.banka4.bank_service.domain.user.User;
import rs.banka4.bank_service.exceptions.*;
import rs.banka4.bank_service.repositories.ActuaryRepository;
import rs.banka4.bank_service.repositories.AssetRepository;
import rs.banka4.bank_service.repositories.OrderRepository;
import rs.banka4.bank_service.repositories.UserRepository;
import rs.banka4.bank_service.service.abstraction.*;
import rs.banka4.bank_service.tx.TxExecutor;
import rs.banka4.bank_service.tx.data.*;
import rs.banka4.rafeisen.common.currency.CurrencyCode;
import rs.banka4.rafeisen.common.exceptions.jwt.Unauthorized;
import rs.banka4.rafeisen.common.security.AuthenticatedBankUserAuthentication;
import rs.banka4.rafeisen.common.security.Privilege;
import rs.banka4.rafeisen.common.security.SecurityUtils;
import rs.banka4.rafeisen.common.security.UserType;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final AssetRepository assetRepository;
    private final ActuaryRepository actuaryRepository;
    private final ListingService listingService;
    private final OrderExecutionService orderExecutionService;
    private final UserRepository userRepository;
    private final AccountService accountService;
    private final ExchangeRateService exchangeRateService;
    private final BankAccountService bankAccountService;
    private final TxExecutor txExecutor;

    @Override
    public OrderDto createOrder(
        CreateOrderDto dto,
        UUID userId,
        AuthenticatedBankUserAuthentication auth
    ) {
        Asset asset =
            assetRepository.findById(dto.assetId())
                .orElseThrow(AssetNotFound::new);

        ActuaryInfo actuaryInfo;
        if (
            auth.getPrincipal()
                .userType()
                == UserType.CLIENT
        ) {
            actuaryInfo = null;
        } else {
            actuaryInfo =
                actuaryRepository.findByUserId(userId)
                    .orElseThrow(() -> new ActuaryNotFoundException(userId));
        }

        OrderType orderType = determineOrderType(dto.limitValue(), dto.stopValue());
        Exchange exchange = resolveExchangeForAsset(asset);

        Listing listing =
            listingService.findActiveListingByAsset(asset.getId())
                .orElseThrow(() -> new ListingNotFoundException(null));

        boolean afterHours =
            OffsetDateTime.now()
                .isAfter(exchange.getCloseTime())
                || OffsetDateTime.now()
                    .isBefore(exchange.getOpenTime());
        BigDecimal pricePerUnit =
            resolveExecutionPrice(
                orderType,
                dto.direction(),
                listing.getAsk(),
                listing.getBid(),
                dto.limitValue(),
                getContractSize(asset)
            );

        boolean needsApproval;

        if (actuaryInfo == null) {
            needsApproval = false;
        } else {
            needsApproval =
                determineIfApprovalNeeded(
                    actuaryInfo,
                    dto.quantity(),
                    getContractSize(asset),
                    pricePerUnit
                );
        }

        Status status = needsApproval ? Status.PENDING : Status.APPROVED;

        User user =
            userRepository.findById(userId)
                .get();

        Order order = OrderMapper.INSTANCE.toEntity(dto);
        order.setUser(user);
        order.setAsset(asset);
        order.setOrderType(orderType);
        order.setQuantity(dto.quantity());
        order.setContractSize(getContractSize(asset));
        order.setPricePerUnit(new MonetaryAmount(pricePerUnit, exchange.getCurrency()));
        order.setStatus(status);
        order.setApprovedBy(null);
        order.setDone(false);
        order.setRemainingPortions(dto.quantity());
        order.setAllOrNothing(dto.allOrNothing());
        order.setAfterHours(afterHours);
        order.setUsed(false);
        order.setAccount(accountService.getAccountByAccountNumber(dto.accountNumber()));

        Order savedOrder = orderRepository.saveAndFlush(order);
        return OrderMapper.INSTANCE.toDto(savedOrder);
    }

    @Override
    public OrderPreviewDto calculateAveragePrice(CreateOrderPreviewDto request) {
        Asset asset =
            assetRepository.findById(request.assetId())
                .orElseThrow(AssetNotFound::new);
        Listing listing =
            listingService.findActiveListingByAsset(asset.getId())
                .orElseThrow(ExchangeNotFound::new);

        OrderType orderType = determineOrderType(request.limitValue(), request.stopValue());
        BigDecimal pricePerUnit =
            resolveExecutionPrice(
                orderType,
                request.direction(),
                listing.getAsk(),
                listing.getBid(),
                request.limitValue(),
                getContractSize(asset)
            );

        String typeLabel = buildOrderTypeLabel(orderType, request.allOrNothing(), request.margin());
        BigDecimal price =
            pricePerUnit.multiply(
                BigDecimal.valueOf((long) request.quantity() * getContractSize(asset))
            );

        Account account = accountService.getAccountByAccountNumber(request.accountNumber());

        if (
            account.getCurrency() != CurrencyCode.RSD
                && listing.getExchange()
                    .getCurrency()
                    != CurrencyCode.RSD
        ) {
            BigDecimal toRsd =
                exchangeRateService.convertCurrency(
                    price,
                    listing.getExchange()
                        .getCurrency(),
                    CurrencyCode.RSD
                );
            price =
                exchangeRateService.convertCurrency(toRsd, CurrencyCode.RSD, account.getCurrency());
        } else {
            price =
                exchangeRateService.convertCurrency(
                    price,
                    listing.getExchange()
                        .getCurrency(),
                    account.getCurrency()
                );
        }


        return new OrderPreviewDto(typeLabel, price, request.quantity());
    }

    @Override
    public Page<OrderDto> searchOrders(List<Status> statuses, Pageable pageable) {
        Page<Order> orders;
        if (statuses == null || statuses.isEmpty()) {
            orders = orderRepository.findAll(pageable);
        } else {
            orders = orderRepository.findAllByStatusIn(statuses, pageable);
        }
        return orders.map(OrderMapper.INSTANCE::toDto);
    }

    @Override
    public void acceptOrder(UUID orderId) {
        Order order =
            orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFound(orderId.toString()));

        if (
            !order.getStatus()
                .equals(Status.PENDING)
        ) throw new AlreadyUpdatedOrderStatus();

        if (hasSettlementDatePassed(order.getAsset())) {
            throw new SettlementDatePassedException();
        }

        order.setStatus(Status.APPROVED);

        orderRepository.save(order);
    }

    @Override
    public void declineOrder(UUID orderId) {
        Order order =
            orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFound(orderId.toString()));

        if (
            !order.getStatus()
                .equals(Status.PENDING)
        ) throw new AlreadyUpdatedOrderStatus();

        order.setStatus(Status.DECLINED);
        order.setDone(true);
        order.setUsed(true);

        orderRepository.save(order);
    }

    @Override
    public OrderDto getOrderById(UUID orderId) {
        return OrderMapper.INSTANCE.toDto(
            orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFound(orderId.toString()))
        );
    }

    @Override
    public void updateOrderStatus(
        UUID orderId,
        Status newStatus,
        AuthenticatedBankUserAuthentication auth
    ) {
        Order order =
            orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFound(orderId.toString()));
        if (
            !auth.getAuthorities()
                .contains(SecurityUtils.asGrantedAuthority(Privilege.SUPERVISOR))
        ) {
            throw new Unauthorized(auth.getToken());
        }

        if (order.isUsed()) {
            throw new AlreadyUpdatedOrderStatus();
        }

        if (newStatus != Status.APPROVED && newStatus != Status.DECLINED) {
            throw new InvalidOrderStatus();
        }

        order.setStatus(newStatus);
        order.setApprovedBy(
            auth.getPrincipal()
                .userId()
        );
        order.setUsed(true);

        orderRepository.save(order);
    }

    private String buildOrderTypeLabel(OrderType type, boolean allOrNone, boolean margin) {
        String label = "";

        if (margin) label += "Margin ";
        if (allOrNone) label += "AON ";

        switch (type) {
        case MARKET -> label += "Market Order";
        case LIMIT -> label += "Limit Order";
        case STOP -> label += "Stop Order";
        case STOP_LIMIT -> label += "Stop-Limit Order";
        }

        return label.trim();
    }

    /**
     * Determine the order type based on the provided limit and stop values. If both are present,
     * it's a STOP_LIMIT order. If only limit is present, it's a LIMIT order. If only stop is
     * present, it's a STOP order. If neither is present, it's a MARKET order.
     */
    private OrderType determineOrderType(MonetaryAmount limit, MonetaryAmount stop) {
        if (limit != null && stop != null) return OrderType.STOP_LIMIT;
        if (limit != null) return OrderType.LIMIT;
        if (stop != null) return OrderType.STOP;
        return OrderType.MARKET;
    }

    private boolean determineIfApprovalNeeded(
        ActuaryInfo actuaryInfo,
        int quantity,
        int contractSize,
        BigDecimal pricePerUnit
    ) {
        if (!actuaryInfo.isNeedApproval()) return false;
        BigDecimal totalCost =
            pricePerUnit.multiply(BigDecimal.valueOf((long) quantity * contractSize));
        return actuaryInfo.getUsedLimit()
            .getAmount()
            .add(totalCost)
            .compareTo(
                actuaryInfo.getLimit()
                    .getAmount()
            )
            > 0;
    }

    /**
     * Check if the settlement date has passed for the given asset (futures/options).
     *
     * @param asset The asset to check.
     * @return true if the settlement date has passed, false otherwise.
     */
    private boolean hasSettlementDatePassed(Asset asset) {
        if (asset instanceof Future) {
            return ((Future) asset).getSettlementDate()
                .isBefore(OffsetDateTime.now());
        } else if (asset instanceof Option) {
            return ((Option) asset).getSettlementDate()
                .isBefore(OffsetDateTime.now());
        }
        return false;
    }

    private Exchange resolveExchangeForAsset(Asset asset) {
        return listingService.findActiveListingByAsset(asset.getId())
            .map(Listing::getExchange)
            .orElseThrow(ExchangeNotFound::new);
    }

    private int getContractSize(Asset asset) {
        return switch (asset) {
        case null -> 1;
        case Option option -> 100;
        case Future future -> (int) future.getContractSize();
        default ->
            // Default for stocks
            1;
        };
    }

    public BigDecimal resolveExecutionPrice(
        OrderType orderType,
        Direction direction,
        BigDecimal askPrice,
        BigDecimal bidPrice,
        MonetaryAmount limit,
        int contractSize
    ) {
        BigDecimal contractSizeBD = BigDecimal.valueOf(contractSize);
        BigDecimal limitValue = limit != null ? limit.getAmount() : null;

        return switch (orderType) {
        case MARKET,
            STOP
            -> resolveMarketOrStopPrice(direction, askPrice, bidPrice, contractSizeBD);
        case LIMIT,
            STOP_LIMIT
            -> resolveLimitOrStopLimitPrice(
                direction,
                askPrice,
                bidPrice,
                limitValue,
                contractSizeBD
            );
        };
    }

    private BigDecimal resolveMarketOrStopPrice(
        Direction direction,
        BigDecimal askPrice,
        BigDecimal bidPrice,
        BigDecimal contractSizeBD
    ) {
        if (direction == Direction.BUY) {
            if (askPrice == null) throw new RequiredPriceException();
            return askPrice.multiply(contractSizeBD);
        } else {
            if (bidPrice == null) throw new RequiredPriceException();
            return bidPrice.multiply(contractSizeBD);
        }
    }

    private BigDecimal resolveLimitOrStopLimitPrice(
        Direction direction,
        BigDecimal askPrice,
        BigDecimal bidPrice,
        BigDecimal limitValue,
        BigDecimal contractSizeBD
    ) {
        if (limitValue == null) throw new RequiredPriceException();

        if (direction == Direction.BUY) {
            if (askPrice != null && askPrice.compareTo(limitValue) < 0) {
                return askPrice.multiply(contractSizeBD);
            } else {
                return limitValue.multiply(contractSizeBD);
            }
        } else {
            if (bidPrice != null && bidPrice.compareTo(limitValue) > 0) {
                return bidPrice.multiply(contractSizeBD);
            } else {
                return limitValue.multiply(contractSizeBD);
            }
        }
    }

    /**
     * This method is called periodically to execute pending orders. It checks the status of each
     * order and executes it if the conditions are met. If the settlement date has passed, the order
     * is declined.
     */
    public void executeOrders() {
        List<Order> ordersToProcess =
            orderRepository.findAllByStatusAndIsDoneFalse(Status.APPROVED);

        for (Order order : ordersToProcess) {
            // Check if settlement date has passed
            if (hasSettlementDatePassed(order.getAsset())) {
                order.setStatus(Status.DECLINED);
                orderRepository.save(order);
                continue;
            }

            switch (order.getOrderType()) {
            case MARKET -> executeMarketOrder(order);
            case LIMIT -> executeLimitOrder(order);
            case STOP -> executeStopOrder(order);
            case STOP_LIMIT -> executeStopLimitOrder(order);
            }

            orderRepository.save(order);
        }
    }

    /**
     * Executes a market order by determining the price based on the current market conditions. The
     * method calculates the total price and commission for the order and processes it as an
     * "All-Or-Nothing" (AON) order or partial order.
     *
     * @param order The order to be executed. Must contain valid asset, direction, and quantity
     *        details.
     * @throws ListingNotFoundException If no active listing is found for the asset associated with
     *         the order.
     */
    private void executeMarketOrder(Order order) {
        Listing listing =
            listingService.findActiveListingByAsset(
                order.getAsset()
                    .getId()
            )
                .orElseThrow(
                    () -> new ListingNotFoundException(
                        order.getAsset()
                            .getId()
                    )
                );

        BigDecimal price =
            (order.getDirection() == Direction.BUY ? listing.getAsk() : listing.getBid()).multiply(
                BigDecimal.valueOf((long) order.getQuantity() * order.getContractSize())
            );

        /*
         * Commission calculation: 14% of the price, capped at 7$.
         */
        BigDecimal commission = price.multiply(BigDecimal.valueOf(0.14));
        if (commission.compareTo(BigDecimal.valueOf(7)) > 0) {
            commission = BigDecimal.valueOf(7);
        }

        System.out.println("Executing market order: " + order.getId());

        if (order.isAllOrNothing()) executeAllOrNothing(order, commission);
        else executePartial(order, commission);
    }

    /**
     * Executes a limit order by checking if the current market price satisfies the limit condition.
     * If the conditions are met, the method calculates the final price and commission for the order
     * and processes it as an "All-Or-Nothing" (AON) order or partial order.
     *
     * @param order The order to be executed. Must contain valid asset, direction, limit value, and
     *        quantity details.
     * @throws ListingNotFoundException If no active listing is found for the asset associated with
     *         the order.
     * @throws RequiredPriceException If the limit value is null or invalid for the order.
     */
    private void executeLimitOrder(Order order) {
        Listing listing =
            listingService.findActiveListingByAsset(
                order.getAsset()
                    .getId()
            )
                .orElseThrow(
                    () -> new ListingNotFoundException(
                        order.getAsset()
                            .getId()
                    )
                );

        BigDecimal limitValue =
            order.getLimitValue()
                .getAmount();
        BigDecimal currentPrice =
            order.getDirection() == Direction.BUY ? listing.getAsk() : listing.getBid();
        /*
         * Check if the current market price satisfies the limit condition. If the order is a buy
         * order, check if the current price is less than or equal to the limit value. If the order
         * is a sell order, check if the current price is greater than or equal to the limit value.
         */
        boolean canExecute =
            (order.getDirection() == Direction.BUY && currentPrice.compareTo(limitValue) <= 0)
                || (order.getDirection() == Direction.SELL
                    && currentPrice.compareTo(limitValue) >= 0);

        if (canExecute) {
            System.out.println("Executing limit order: " + order.getId());
            BigDecimal finalPrice;
            if (order.getDirection() == Direction.BUY && currentPrice.compareTo(limitValue) < 0) {
                finalPrice = currentPrice;
            } else
                if (
                    order.getDirection() == Direction.SELL && currentPrice.compareTo(limitValue) > 0
                ) {
                    finalPrice = currentPrice;
                } else {
                    finalPrice = limitValue;
                }
            finalPrice =
                finalPrice.multiply(
                    BigDecimal.valueOf((long) order.getQuantity() * order.getContractSize())
                );

            /*
             * Commission calculation: 24% of the price, capped at 12$.
             */
            BigDecimal commission = finalPrice.multiply(BigDecimal.valueOf(0.24));
            if (commission.compareTo(BigDecimal.valueOf(12)) > 0) {
                commission = BigDecimal.valueOf(12);
            }

            if (order.isAllOrNothing()) executeAllOrNothing(order, commission);
            else executePartial(order, commission);
        }
    }

    /**
     * Executes a stop order by checking if the current market price satisfies the stop condition.
     * If the stop condition is met, the order is converted to a market order and executed.
     *
     * @param order The order to be executed. Must contain valid asset, direction, and stop value
     *        details.
     * @throws ListingNotFoundException If no active listing is found for the asset associated with
     *         the order.
     * @throws RequiredPriceException If the stop value is null or invalid for the order.
     */
    private void executeStopOrder(Order order) {
        Listing listing =
            listingService.findActiveListingByAsset(
                order.getAsset()
                    .getId()
            )
                .orElseThrow(
                    () -> new ListingNotFoundException(
                        order.getAsset()
                            .getId()
                    )
                );

        BigDecimal stopValue =
            order.getStopValue()
                .getAmount();
        BigDecimal currentPrice =
            order.getDirection() == Direction.BUY ? listing.getAsk() : listing.getBid();

        boolean shouldTrigger =
            (order.getDirection() == Direction.BUY && currentPrice.compareTo(stopValue) >= 0)
                || (order.getDirection() == Direction.SELL
                    && currentPrice.compareTo(stopValue) <= 0);

        if (shouldTrigger) {
            order.setOrderType(OrderType.MARKET);
            executeMarketOrder(order);
        }
    }

    /**
     * Executes a stop-limit order by checking if the current market price satisfies the stop
     * condition. If the stop condition is met, the order is converted to a limit order for further
     * execution.
     *
     * @param order The order to be executed. Must contain valid asset, direction, stop value, and
     *        limit value details.
     * @throws ListingNotFoundException If no active listing is found for the asset associated with
     *         the order.
     * @throws RequiredPriceException If the stop value or limit value is null or invalid for the
     *         order.
     */
    private void executeStopLimitOrder(Order order) {
        Listing listing =
            listingService.findActiveListingByAsset(
                order.getAsset()
                    .getId()
            )
                .orElseThrow(
                    () -> new ListingNotFoundException(
                        order.getAsset()
                            .getId()
                    )
                );

        BigDecimal stopValue =
            order.getStopValue()
                .getAmount();
        BigDecimal currentPrice =
            order.getDirection() == Direction.BUY ? listing.getAsk() : listing.getBid();

        boolean shouldTrigger =
            (order.getDirection() == Direction.BUY && currentPrice.compareTo(stopValue) >= 0)
                || (order.getDirection() == Direction.SELL
                    && currentPrice.compareTo(stopValue) <= 0);

        if (!shouldTrigger) return;

        order.setOrderType(OrderType.LIMIT);
        executeLimitOrder(order);
    }

    /**
     * If the order is AON and cannot be filled completely system will wait for future execution if
     * the market conditions change.
     */
    private void executeAllOrNothing(Order order, BigDecimal commission) {
        orderExecutionService.processAllOrNothingOrderAsync(order)
            .thenAccept(executed -> {
                if (executed) {
                    payFee(order, commission);
                }
            })
            .exceptionally(ex -> null);
    }

    /**
     * Executes an order in partial chunks by finding and matching available orders until the entire
     * order is fulfilled or no further matches are found. If the execution completes successfully,
     * a fee is paid based on the provided commission.
     *
     * @param order The order to be executed in partial chunks. Must contain valid asset, direction,
     *        and quantity details.
     * @param commission The calculated commission for the partial order execution.
     * @throws RuntimeException If an interruption or execution error occurs during the processing
     *         of the order.
     */
    private void executePartial(Order order, BigDecimal commission) {
        orderExecutionService.processPartialOrderAsync(order)
            .thenAccept(executed -> {
                if (executed) {
                    payFee(order, commission);
                }
            })
            .exceptionally(ex -> null);
    }


    private void payFee(Order order, BigDecimal commission) {
        StockDescription stockDescription =
            new StockDescription(
                order.getAsset()
                    .getTicker()
            );

        Account bankAccount =
            bankAccountService.getBankAccountForCurrency(
                order.getAccount()
                    .getCurrency()
            );

        Posting orderPosting =
            new Posting(
                new TxAccount.Account(
                    order.getAccount()
                        .getAccountNumber()
                ),
                commission.negate(),
                new TxAsset.Stock(stockDescription)
            );

        Posting bankPosting =
            new Posting(
                new TxAccount.Account(bankAccount.getAccountNumber()),
                commission,
                new TxAsset.Stock(stockDescription)
            );

        DoubleEntryTransaction transaction =
            new DoubleEntryTransaction(
                List.of(orderPosting, bankPosting),
                "Order execution fee",
                ForeignBankId.our(UUID.randomUUID())
            );

        txExecutor.submitImmediateTx(transaction);
    }

}
