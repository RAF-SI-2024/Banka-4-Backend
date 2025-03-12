package rs.banka4.user_service.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import rs.banka4.user_service.domain.card.dtos.CardDto;
import rs.banka4.user_service.domain.card.dtos.CreateAuthorizedUserDto;
import rs.banka4.user_service.domain.card.dtos.CreateCardDto;

import java.util.UUID;

@Tag(name = "CardDocumentation", description = "Endpoints for card functionalities")
public interface CardDocumentation {
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "Card created successfully",
                            content = @Content(schema = @Schema(implementation = UUID.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request format",
                            content = @Content(examples = {
                                    @ExampleObject(name = "Missing fields", value = """
                                            {"message":"Validation failed","errors":["accountNumber is mandatory"]}""")
                            })),
                    @ApiResponse(responseCode = "401", description = "Authentication failure",
                            content = @Content(examples = {
                                    @ExampleObject(name = "Invalid 2FA", value = """
                                            {"message":"Invalid TOTP code","code":"AUTH_002"}""")
                            })),
                    @ApiResponse(responseCode = "403", description = "Business rule violation",
                            content = @Content(examples = {
                                    @ExampleObject(name = "Card limit", value = """
                                            {"message":"Personal account limit: 2 cards","code":"CARD_001"}"""),
                                    @ExampleObject(name = "Duplicate auth", value = """
                                            {"message":"User already has card","code":"CARD_002"}""")
                            })),
                    @ApiResponse(responseCode = "404", description = "Account not found",
                            content = @Content(examples = @ExampleObject(
                                    value = """
                                            {"message":"Account not found","code":"ACC_001"}
                                            """)))
            }
    )
    ResponseEntity<UUID> createAuthorizedCard(Authentication auth, CreateCardDto createCardDto);

    @Operation(
            summary = "This endpoint is used to block existing card",
            description = "This endpoint is used to block existing card. Client can block their own card.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Card successfully blocked"),
                    @ApiResponse(responseCode = "400", description = "Invalid card data"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Card privileges required"),
            }
    )
    ResponseEntity<Void> blockCard(Authentication authentication, String cardNumber);

    @Operation(
            summary = "This endpoint is used to unblock existing card",
            description = "This endpoint is used to block existing card. Client can block their own card.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Card successfully unblocked"),
                    @ApiResponse(responseCode = "400", description = "Invalid card data"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Employee privileges required"),
            }
    )
    ResponseEntity<Void> unblockCard(Authentication authentication, String cardNumber);

    @Operation(
            summary = "This endpoint is used to deactivate existing card",
            description = "This endpoint is used to deactivate existing card. Client can deactivate their own card.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Card successfully deactivate"),
                    @ApiResponse(responseCode = "400", description = "Invalid card data"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Card privileges required"),
            }
    )
    ResponseEntity<Void> deactivateCard( Authentication authentication, String cardNumber);

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
