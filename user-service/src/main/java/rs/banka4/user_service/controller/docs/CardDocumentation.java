package rs.banka4.user_service.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.domain.card.dtos.CreateAuthorizedCardDto;

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
                            content = @Content(schema = @Schema(implementation = CreateAuthorizedCardDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid data or card limit exceeded"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized or invalid 2FA code"),
            }
    )
    ResponseEntity<Void> createAuthorizedCard(CreateAuthorizedCardDto createAuthorizedCardDto);
}
