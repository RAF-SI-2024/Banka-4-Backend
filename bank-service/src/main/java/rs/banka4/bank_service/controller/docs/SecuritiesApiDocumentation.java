package rs.banka4.bank_service.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestParam;
import rs.banka4.bank_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.bank_service.domain.actuaries.db.dto.ActuaryProfitDto;
import rs.banka4.bank_service.domain.security.SecurityDto;
import rs.banka4.bank_service.domain.security.forex.dtos.ForexPairDto;
import rs.banka4.bank_service.domain.security.future.dtos.FutureDto;
import rs.banka4.bank_service.domain.security.responses.SecurityHoldingDto;
import rs.banka4.bank_service.domain.security.stock.dtos.StockDto;
import rs.banka4.bank_service.domain.taxes.db.dto.UserTaxInfoDto;

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
        summary = "Get My Profit",
        description = "Retrieves the total (unrealized) profit for the authenticated user based on their current holdings. "
            + "Only users with valid bearer tokens can access this endpoint.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved user's profit",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MonetaryAmount.class)
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
    ResponseEntity<MonetaryAmount> getMyProfit(Authentication auth);

    @Operation(
        summary = "Get My Portfolio",
        description = "Retrieves a paginated list of the user's current holdings, including profit information "
            + "calculated from buy orders and the current listing price. Only accessible by authenticated users.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved user's portfolio holdings",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SecurityHoldingDto.class)
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
    ResponseEntity<Page<SecurityHoldingDto>> getMyPortfolio(
        Authentication auth,
        @Parameter(description = "Page number (defaults to 0)")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Number of holdings per page (defaults to 10)")
        @RequestParam(defaultValue = "10") int size
    );

    @Operation(
        summary = "Get My Tax",
        description = "Retrieves the user's tax information for their account in RSD. "
            + "This includes the total tax paid for the current year and the outstanding tax for the current month. "
            + "Accessible only by authenticated users.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved user's tax information",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserTaxInfoDto.class)
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
    public ResponseEntity<UserTaxInfoDto> getMyTax(Authentication auth);


    @Operation(
        summary = "Retrieve Actuary Profit Summary",
        description = "Returns a paginated list of actuaries along with their total realized profit in RSD, "
            + "calculated from all completed sell orders (including partially filled). "
            + "Supports filtering by first name, last name, position, and email.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved profit summary",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ActuaryProfitDto.class)
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
    ResponseEntity<Page<ActuaryProfitDto>> getBankProfit(
        @RequestParam(required = false) String firstName,
        @RequestParam(required = false) String lastName,
        @RequestParam(required = false) String position,
        @RequestParam(required = false) String email,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    );

}
