package rs.banka4.user_service.domain.card.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import javax.annotation.Nullable;

public record CreateCardDto(
    @Schema(
        description = "Account number",
        example = "1234567890123456"
    ) @NotBlank String accountNumber,

    @Nullable
    @Schema(
        description = "Authorized user for business card"
    ) CreateAuthorizedUserDto authorizedUser,

    @NotBlank(message = "TOTP code content is required")
    @NotNull(message = "TOTP code cannot be null") String otpCode
) {
}
