package rs.banka4.user_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for company details")
public record CompanyDto(
        @Schema(description = "Company ID", example = "cccccccc-4444-dddd-5555-eeee6666ffff")
        String id,
        @Schema(description = "Name", example = "Acme Corp")
        String name,
        @Schema(description = "TIN", example = "123456789")
        String tin,
        @Schema(description = "CRN", example = "987654321")
        String crn,
        @Schema(description = "Address", example = "123 Main St")
        String address
) { }
