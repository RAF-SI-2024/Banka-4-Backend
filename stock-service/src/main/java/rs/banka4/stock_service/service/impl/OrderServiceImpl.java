package rs.banka4.stock_service.service.impl;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rs.banka4.rafeisen.common.currency.CurrencyCode;
import rs.banka4.stock_service.domain.actuaries.db.ActuaryInfo;
import rs.banka4.stock_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.stock_service.domain.exchanges.db.Exchange;
import rs.banka4.stock_service.domain.listing.db.Listing;
import rs.banka4.stock_service.domain.options.db.Asset;
import rs.banka4.stock_service.domain.options.db.Option;
import rs.banka4.stock_service.domain.orders.db.Direction;
import rs.banka4.stock_service.domain.orders.db.Order;
import rs.banka4.stock_service.domain.orders.db.OrderType;
import rs.banka4.stock_service.domain.orders.db.Status;
import rs.banka4.stock_service.domain.orders.dtos.CreateOrderDto;
import rs.banka4.stock_service.domain.orders.dtos.CreateOrderPreviewDto;
import rs.banka4.stock_service.domain.orders.dtos.OrderDto;
import rs.banka4.stock_service.domain.orders.dtos.OrderPreviewDto;
import rs.banka4.stock_service.domain.orders.mapper.OrderMapper;
import rs.banka4.stock_service.domain.security.future.db.Future;
import rs.banka4.stock_service.exceptions.*;
import rs.banka4.stock_service.repositories.ActuaryRepository;
import rs.banka4.stock_service.repositories.AssetRepository;
import rs.banka4.stock_service.repositories.OrderRepository;
import rs.banka4.stock_service.service.abstraction.ListingService;
import rs.banka4.stock_service.service.abstraction.OrderService;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final AssetRepository assetRepository;
    private final ActuaryRepository actuaryRepository;
    private final ListingService listingService;

    @Override
    public OrderDto createOrder(CreateOrderDto dto, UUID userId) {
        Asset asset =
            assetRepository.findById(dto.assetId())
                .orElseThrow(AssetNotFound::new);

        ActuaryInfo actuaryInfo =
            actuaryRepository.findByUserId(userId)
                .orElseThrow(() -> new ActuaryNotFoundException(userId));

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

        boolean needsApproval =
            determineIfApprovalNeeded(
                actuaryInfo,
                dto.quantity(),
                getContractSize(asset),
                pricePerUnit
            );
        Status status = needsApproval ? Status.PENDING : Status.APPROVED;

        Order order = OrderMapper.INSTANCE.toEntity(dto);
        order.setUserId(userId);
        order.setAsset(asset);
        order.setOrderType(orderType);
        order.setQuantity(dto.quantity());
        order.setContractSize(getContractSize(asset));
        order.setPricePerUnit(new MonetaryAmount(pricePerUnit, CurrencyCode.RSD));
        order.setStatus(status);
        order.setApprovedBy(null);
        order.setDone(false);
        order.setRemainingPortions(dto.quantity());
        order.setAfterHours(afterHours);
        order.setUsed(false);

        Order savedOrder = orderRepository.saveAndFlush(order);
        return OrderMapper.INSTANCE.toDto(savedOrder);
    }

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

        return new OrderPreviewDto(typeLabel, price, request.quantity());
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

}
