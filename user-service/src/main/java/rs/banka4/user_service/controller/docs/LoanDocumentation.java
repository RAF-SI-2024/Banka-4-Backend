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
import rs.banka4.user_service.domain.loan.dtos.LoanApplicationDto;

@Tag(name = "LoanDocumentation", description = "Endpoints for loan functionalities")
public interface LoanDocumentation {
    @Operation(
            summary = "Client Credit Request",
            description = "Allows a client to request credit for their account. " +
                    "This endpoint validates the request using the ClientCreditRequestDto.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Credit request processed successfully",
                            content = @Content(schema = @Schema(implementation = LoanApplicationDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data or business rule violation"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized: Client authentication failed"),
            }
    )
    ResponseEntity<Void> createLoanApplication(LoanApplicationDto loanApplicationDto);
}
