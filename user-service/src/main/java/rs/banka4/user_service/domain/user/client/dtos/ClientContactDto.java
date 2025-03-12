package rs.banka4.user_service.domain.user.client.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record ClientContactDto(
    @Schema(
        description = "Client Contact ID",
        example = "123e4567-e89b-12d3-a456-426614174000"
    ) UUID id,
    @Schema(
        description = "Client Contact nickname",
        example = "Wasketov racun od firme"
    ) String nickname,
    @Schema(
        description = "Account number",
        example = "444000000000123456"
    ) String accountNumber
) {
}
