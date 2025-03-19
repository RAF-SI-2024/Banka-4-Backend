package rs.banka4.user_service.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import rs.banka4.user_service.domain.card.dtos.CardDto;
import rs.banka4.user_service.domain.card.dtos.CreateCardDto;

@Tag(
    name = "CardDocumentation",
    description = "Endpoints for card functionalities"
)
public interface CardDocumentation {
    @Operation(
        summary = "Create a new card",
        description = "Creates a new debit card for the specified account with optional authorized user. Requires 2FA OTP."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Card created successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid OTP or request data"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Unauthorized access"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Account not found"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Card limit exceeded or duplicate authorized user"
        )
    })
    ResponseEntity<Void> createAuthorizedCard(Authentication auth, CreateCardDto createCardDto);

    @Operation(
        summary = "This endpoint is used to block existing card",
        description = "This endpoint is used to block existing card. Client can block their own card.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Card successfully blocked"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid card data"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - Card privileges required"
            ),
        }
    )
    ResponseEntity<Void> blockCard(Authentication authentication, String cardNumber);

    @Operation(
        summary = "This endpoint is used to unblock existing card",
        description = "This endpoint is used to block existing card. Client can block their own card.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Card successfully unblocked"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid card data"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - Employee privileges required"
            ),
        }
    )
    ResponseEntity<Void> unblockCard(Authentication authentication, String cardNumber);

    @Operation(
        summary = "This endpoint is used to deactivate existing card",
        description = "This endpoint is used to deactivate existing card. Client can deactivate their own card.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Card successfully deactivate"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid card data"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - Card privileges required"
            ),
        }
    )
    ResponseEntity<Void> deactivateCard(Authentication authentication, String cardNumber);

    @Operation(
        summary = "This endpoint is used to return all cards for specific accountNumber filter",
        description = "This endpoint is used to return all cards for specific accountNumber filter."
            + "Client uses this endpoint on their own cards. The response is pageable.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved the list of cards",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CardDto.class)
                )
            ),
            @ApiResponse(
                responseCode = "204",
                description = "No cards found for the provided account number"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid request parameters"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Access forbidden - User lacks required permissions"
            ),
        }
    )
    ResponseEntity<Page<CardDto>> clientSearchCards(Authentication auth, String accountNumber, int page, int size);

    @Operation(
        summary = "This endpoint is used to return all cards for specific cardNumber, firstName,"
            + "lastName, email and cardStatus filter",
        description = "This endpoint is used to return all cards for specific cardNumber, firstName,"
            + "lastName, email and cardStatus filter"
            + "Employee uses this endpoint on all cards. The response is pageable.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved the list of cards",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CardDto.class)
                )
            ),
            @ApiResponse(
                responseCode = "204",
                description = "No cards found for the provided account number"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid request parameters"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Access forbidden - User lacks required permissions"
            ),
        }
    )
    ResponseEntity<Page<CardDto>> employeeSearchCards(
        Authentication auth,
        String cardNumber,
        String firstName,
        String lastName,
        String email,
        String cardStatus,
        int page,
        int size
    );
}
