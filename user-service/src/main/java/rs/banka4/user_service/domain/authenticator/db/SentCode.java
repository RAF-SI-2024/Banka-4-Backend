package rs.banka4.user_service.domain.authenticator.db;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SentCode(
        @Schema(description = "TOTP code", example = "123 456")
        @NotBlank(message = "code content is required")
        @NotNull
        String content) {
}
