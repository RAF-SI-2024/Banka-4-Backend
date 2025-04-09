package rs.banka4.stock_service.domain.orders.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import rs.banka4.stock_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.stock_service.domain.orders.db.Direction;

@Schema(description = "DTO for order preview")
public record CreateOrderPreviewDto(
    @Schema(
        description = "Asset ID",
        example = "1de54a3a-d879-4154-aa3a-e40598186f93"
    ) @NotNull(message = "AssetId is required") UUID assetId,
    @Schema(
        description = "Quantity",
        example = "1"
    ) @NotNull(message = "Quantity is required") int quantity,
    MonetaryAmount limitValue,
    MonetaryAmount stopValue,
    boolean allOrNothing,
    boolean margin,
    @Schema(
        description = "Order direction (BUY or SELL)",
        example = "BUY"
    ) @NotNull(message = "Direction is required") Direction direction
) {
}
