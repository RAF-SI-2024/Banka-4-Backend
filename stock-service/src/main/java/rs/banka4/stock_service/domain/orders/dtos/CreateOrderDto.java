package rs.banka4.stock_service.domain.orders.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import rs.banka4.stock_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.stock_service.domain.orders.db.Direction;

@Schema(description = "DTO for creating order")
public record CreateOrderDto(
    @Schema(
        description = "User ID",
        example = "1de54a3a-d879-4154-aa3a-e40598186f93"
    ) @NotNull(message = "AssetId is required") UUID assetId,
    @Schema(
        description = "Order direction (BUY or SELL)",
        example = "BUY"
    ) @NotNull(message = "Direction is required") Direction direction,
    @Schema(
        description = "Order quantity",
        example = "1"
    ) @NotNull(message = "Quantity is required") int quantity,
    @Schema(
        description = "Price per unit",
        example = "1000.0"
    ) MonetaryAmount pricePerUnit,
    @Schema(
        description = "Order limit value",
        example = "1000.0"
    ) MonetaryAmount limitValue,
    @Schema(
        description = "Order stop value",
        example = "1000.0"
    ) MonetaryAmount stopValue,
    @Schema(
        description = "All or nothing flag",
        example = "true"
    ) boolean allOrNothing,
    @Schema(
        description = "Margin flag",
        example = "true"
    ) boolean margin,
    @Schema(
        description = "Account ID",
        example = "1de54a3a-d879-4154-aa3a-e40598186f93"
    ) UUID accountId
) {
}
