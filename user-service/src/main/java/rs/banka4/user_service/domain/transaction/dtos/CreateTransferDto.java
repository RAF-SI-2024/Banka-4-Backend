package rs.banka4.user_service.domain.transaction.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateTransferDto(
    @Schema(
        description = "From account number",
        example = "102-39443942389"
    ) String fromAccount,
    @Schema(
        description = "To account number",
        example = "102-394438340549"
    ) String toAccount,
    @Schema(
        description = "From amount",
        example = "1.00"
    ) BigDecimal fromAmount,
    @NotBlank(message = "TOTP code content is required")
    @NotNull(message = "TOTP code cannot be null") String otpCode
) {
}
