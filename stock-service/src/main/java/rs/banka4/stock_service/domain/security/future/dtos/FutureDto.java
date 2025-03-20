package rs.banka4.stock_service.domain.security.future.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import rs.banka4.stock_service.domain.security.future.db.UnitName;

public record FutureDto(
    @Schema(
        description = "Future contract ID",
        example = "123e4567-e89b-12d3-a456-426614174000"
    ) UUID id,

    @Schema(
        description = "Human readable name",
        example = "Texas Corn",
        nullable = true
    ) String name,

    @Schema(
        description = "Size of the future contract",
        example = "1000"
    ) long contractSize,

    @Schema(
        description = "Unit of the contract size",
        example = "BARREL"
    ) UnitName contractUnit,

    @Schema(
        description = "Settlement date of the future contract",
        example = "2025-12-31"
    ) OffsetDateTime settlementDate,

    @Schema(
        description = "Current price of the future contract",
        example = "75.50",
        nullable = true
    ) BigDecimal price
) {
}
