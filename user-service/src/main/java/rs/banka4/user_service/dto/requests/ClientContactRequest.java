package rs.banka4.user_service.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

public record ClientContactRequest(
        @Schema(description = "Client Account number", example = "444000000000123456")
        String accountNumber
) {}