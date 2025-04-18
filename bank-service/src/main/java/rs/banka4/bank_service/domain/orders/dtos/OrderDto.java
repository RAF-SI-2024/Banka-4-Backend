package rs.banka4.bank_service.domain.orders.dtos;

import java.time.OffsetDateTime;
import java.util.UUID;
import rs.banka4.bank_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.bank_service.domain.orders.db.Direction;
import rs.banka4.bank_service.domain.orders.db.OrderType;
import rs.banka4.bank_service.domain.orders.db.Status;

public record OrderDto(
    UUID id,
    String firstName,
    String lastName,
    String assetTicker,
    OrderType orderType,
    Direction direction,
    int quantity,
    MonetaryAmount pricePerUnit,
    Status status,
    boolean isDone,
    OffsetDateTime createdAt,
    OffsetDateTime lastModified,
    int contractSize,
    int remainingPortions
) {
}
