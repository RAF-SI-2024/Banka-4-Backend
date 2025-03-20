package rs.banka4.stock_service.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import rs.banka4.stock_service.domain.listing.dtos.ListingDto;

public interface ListingApiDocumentation {
    @Operation(
        summary = "Search Listings",
        description = "Retrieves listings based on the security type filter and pagination. User can see listings"
            + "that are allowed for his role.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved listings",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ListingDto.class)
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
    ResponseEntity<Page<ListingDto>> getListings(
        @Parameter(description = "Type of security to filter by") String securityType,
        @Parameter(description = "Page number") int page,
        @Parameter(description = "Number of listings per page") int size
    );
}
