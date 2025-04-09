package rs.banka4.stock_service.domain.assets.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import rs.banka4.stock_service.service.impl.TransferTo;

@Schema(description = "DTO for transfering stocks from private to public portal and vice versa")
public record TransferDto(
    @NotNull UUID stockId,
    @NotNull int amount,
    @Schema(
        description = "If this field is PUBLIC, that means user request is to transfer stocks from private to public, other way around for PRIVATE"
    ) @NotNull TransferTo transferTo
) {
}
