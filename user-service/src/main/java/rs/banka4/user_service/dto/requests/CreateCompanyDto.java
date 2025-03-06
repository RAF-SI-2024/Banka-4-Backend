package rs.banka4.user_service.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreateCompanyDto (
    @Schema(description = "Name", example = "Acme Corp")
    String name,
    @Schema(description = "TIN", example = "123456789")
    String tin,
    @Schema(description = "CRN", example = "987654321")
    String crn,
    @Schema(description = "Address", example = "123 Main St")
    @NotBlank
    String address,
    @Schema(description = "Activity Code", example = "441100")
    String activityCode
){}
