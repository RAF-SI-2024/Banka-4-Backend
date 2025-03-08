package rs.banka4.user_service.domain.user.client.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ClientContactRequest(
        @Schema(description = "Client Account nickname", example = "Wasketov racun od firme")
        @NotBlank(message = "Nickname is required")
        String nickname,

        @Schema(description = "Client Account number", example = "444000000000123456")
        @NotBlank(message = "Account number for client is required")
        String accountNumber
) {}
