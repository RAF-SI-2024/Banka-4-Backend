package rs.banka4.user_service.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.domain.card.dtos.CardDto;
import rs.banka4.user_service.domain.card.dtos.CreateAuthorizedUserDto;

import java.util.UUID;

@Tag(name = "CardDocumentation", description = "Endpoints for card functionalities")
public interface CardDocumentation {
    @Operation(
            summary = "Card Creation with 2FA",
            description = "Creates a new debit card for a user’s account while enforcing account-specific card limits. " +
                    "For personal accounts, a maximum of 2 cards is allowed, and for business accounts, only 1 card per person. " +
                    "This endpoint initiates a 2-factor authentication process by sending a confirmation code via email. " +
                    "Once the client verifies the code, a new card (with a 16-digit card number and 3-digit CVV) " +
                    "is created and linked to the provided account number. The request payload should include the " +
                    "account number and optionally an authorized user’s details. If no authorized user is provided, " +
                    "the field will be stored as null.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Card successfully created",
                            content = @Content(schema = @Schema(implementation = CreateAuthorizedUserDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid data or card limit exceeded"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized or invalid 2FA code"),
            }
    )
    ResponseEntity<UUID> createAuthorizedCard(CreateAuthorizedUserDto createAuthorizedCardDto);

    @Operation(
            summary = "This endpoint is used to block existing card",
            description = "This endpoint is used to block existing card. Client can block their own card.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Card successfully blocked"),
                    @ApiResponse(responseCode = "400", description = "Invalid card data"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Card privileges required"),
            }
    )
    ResponseEntity<Void> blockCard(String cardNumber);

    @Operation(
            summary = "This endpoint is used to unblock existing card",
            description = "This endpoint is used to block existing card. Client can block their own card.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Card successfully unblocked"),
                    @ApiResponse(responseCode = "400", description = "Invalid card data"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Employee privileges required"),
            }
    )
    ResponseEntity<Void> unblockCard(String cardNumber);

    @Operation(
            summary = "This endpoint is used to deactivate existing card",
            description = "This endpoint is used to deactivate existing card. Client can deactivate their own card.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Card successfully deactivate"),
                    @ApiResponse(responseCode = "400", description = "Invalid card data"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Card privileges required"),
            }
    )
    ResponseEntity<Void> deactivateCard(String cardNumber);

    @Operation(
            summary = "This endpoint is used to return all cards for specific accountNumber filter",
            description = "This endpoint is used to return all cards for specific accountNumber filter." +
                    "Client uses this endpoint on their own cards. The response is pageable.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of cards",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CardDto.class))),
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
    ResponseEntity<Page<CardDto>> clientSearchCards(String accountNumber, int page, int size);

    @Operation(
            summary = "This endpoint is used to return all cards for specific cardNumber, firstName," +
                    "lastName, email and cardStatus filter",
            description = "This endpoint is used to return all cards for specific cardNumber, firstName," +
                    "lastName, email and cardStatus filter" +
                    "Employee uses this endpoint on all cards. The response is pageable.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of cards",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CardDto.class))),
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
    ResponseEntity<Page<CardDto>> employeeSearchCards(String cardNumer, String firstName, String lastName,
                                                      String email, String cardStatus, int page, int size);
}
