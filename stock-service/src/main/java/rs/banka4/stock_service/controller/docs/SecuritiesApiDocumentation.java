package rs.banka4.stock_service.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import rs.banka4.stock_service.domain.security.SecurityDto;
import rs.banka4.stock_service.domain.security.forex.dtos.ForexPairDto;
import rs.banka4.stock_service.domain.security.future.dtos.FutureDto;
import rs.banka4.stock_service.domain.security.responses.SecurityOwnershipResponse;
import rs.banka4.stock_service.domain.security.stock.dtos.StockDto;

public interface SecuritiesApiDocumentation {

    @Operation(
        summary = "Search Securities",
        description = "Retrieves securities based on the security type and name filter. User can see securities"
            + "that are allowed for his role.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved securities",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(anyOf = {
                        StockDto.class,FutureDto.class,ForexPairDto.class
                    })
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid request parameters"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden"
            )
        }
    )
    ResponseEntity<Page<SecurityDto>> getSecurities(
        @Parameter(description = "Type of security to filter by") String securityType,
        @Parameter(description = "Name of the security") String name,
        @Parameter(description = "Page number") int page,
        @Parameter(description = "Number of securities per page") int size
    );

    @Operation(
        summary = "Get my securities holdings",
        description = "Returns a list of all securities currently owned by the authenticated user along with financial summaries.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved securities",
                content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                        schema = @Schema(implementation = SecurityOwnershipResponse.class)
                    )
                )
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden"
            )
        }
    )
    public ResponseEntity<List<SecurityOwnershipResponse>> getMySecurities(Authentication authentication);

}
