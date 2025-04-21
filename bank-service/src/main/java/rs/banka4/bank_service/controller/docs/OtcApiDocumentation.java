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
import rs.banka4.bank_service.domain.trading.db.dtos.OtcRequestCreateDto;
import rs.banka4.bank_service.domain.trading.db.dtos.OtcRequestDto;
import rs.banka4.bank_service.domain.trading.db.dtos.OtcRequestUpdateDto;

public interface OtcApiDocumentation {
    @Operation(
        summary = "Get My Requests",
        description = "Retrieves negotiations in which the authenticated user participates, either as the initiator or the recipient.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved negotiations",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = OtcRequestDto.class)
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
    ResponseEntity<Page<OtcRequestDto>> getMyRequests(
        @Parameter(description = "Page number") int page,
        @Parameter(description = "Number of listings per page") int size,
        Authentication auth
    );

    @Operation(
        summary = "Get My Unread Requests",
        description = "Retrieves unread negotiations (those where the last modification was not done by the authenticated user). "
            + "This enables the user to see the negotiations in which a response or further action is pending.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved unread negotiations",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = OtcRequestDto.class)
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
    ResponseEntity<Page<OtcRequestDto>> getMyRequestsUnread(
        @Parameter(description = "Page number") int page,
        @Parameter(description = "Number of listings per page") int size,
        Authentication auth
    );

    @Operation(
        summary = "Reject OTC Request",
        description = "Rejects an OTC negotiation request identified by its unique request ID. "
            + "When a request is rejected, it is canceled and no further action is taken on that negotiation.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully rejected the OTC request"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid request parameters or request ID format"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "OTC request not found"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden"
            )
        }
    )
    ResponseEntity<Void> rejectOtcRequest(
        @Parameter(description = "id of the otc request") Long idId,
        @Parameter(description = "routing num of the bank") String routingNumber

    );

    @Operation(
        summary = "Update OTC Request",
        description = "Updates an existing OTC request negotiation with new values for price per stock, premium, amount, and settlement date. "
            + "The request status will be updated based on the business rules (for example, rejected if certain conditions are met).",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "OTC request successfully updated"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid request parameters"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "OTC request not found"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden"
            )
        }
    )
    ResponseEntity<Void> updateOtcRequest(
        @Parameter(
            description = "Data Transfer Object representing the update information for the OTC request negotiation"
        ) OtcRequestUpdateDto otcRequestUpdateDto,

        @Parameter(description = "id of the otc request") Long idId,
        @Parameter(description = "routing num of the bank") String routingNumber,
        Authentication auth
    );

    @Operation(
        summary = "Create OTC Request",
        description = "Creates a new OTC request negotiation for trading securities. <br/>"
            + "This operation verifies that the asset ownership (sum of public and reserved amounts) "
            + "is sufficient for the requested amount. If the validation passes, "
            + "an OTC request is created with the authenticated user as the initiator. "
            + "Additional details are obtained from the asset ownership and the bank routing information "
            + "to generate the final OTC request in the ACTIVE state.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "OTC request successfully created"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid request parameters or insufficient asset ownership"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden"
            )
        }
    )
    ResponseEntity<Void> createOtcRequest(
        @Parameter(
            description = "Data Transfer Object representing the create information for the OTC request negotiation"
        ) OtcRequestCreateDto otcRequestCreateDto,
        Authentication auth
    );

    @Operation(
        summary = "Accept OTC Request",
        description = "Accepts otc request and makes an option for buyer, seller gets his premium money from buyer in exchange",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "OTC request successfully accepted"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "OTC request not found"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden"
            ),
            @ApiResponse(
                responseCode = "503",
                description = "Transaction service not implemented yet"
            )
        }
    )
    ResponseEntity<Void> acceptOtcRequest(
        @Parameter(description = "id of the otc request") Long idId,
        @Parameter(description = "routing num of the bank") String routingNumber,
        Authentication auth
    );
}
