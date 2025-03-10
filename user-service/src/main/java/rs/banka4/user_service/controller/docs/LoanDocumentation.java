package rs.banka4.user_service.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import rs.banka4.user_service.domain.loan.dtos.LoanApplicationDto;
import rs.banka4.user_service.domain.loan.dtos.LoanInformationDto;
import rs.banka4.user_service.exceptions.loan.LoanNotFound;

@Tag(name = "LoanDocumentation", description = "Endpoints for loan functionalities")
public interface LoanDocumentation {
    @Operation(
            summary = "Client Loan Request",
            description = "Allows a client to request loan for their account. " +
                    "This endpoint validates the request using the LoanApplicationDto.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Loan request processed successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input data or business rule violation"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized: Client authentication failed"),
            }
    )
    ResponseEntity<Void> createLoanApplication(LoanApplicationDto loanApplicationDto);

    @Operation(
            summary = "Search All Loans",
            description = "Allows an employee to search and retrieve all loans in the system.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Loans retrieved successfully",
                            content = @Content(schema = @Schema(implementation = LoanInformationDto.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized: Employee authentication failed"),
                    @ApiResponse(responseCode = "403", description = "Forbidden: Insufficient permissions")
            }
    )
    ResponseEntity<Page<LoanInformationDto>> getAllLoans(int page, int size);

    @Operation(
            summary = "Search Client's Loans",
            description = "Retrieves loans for authenticated client using the token provided in the Authorization header.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Loans retrieved successfully",
                            content = @Content(schema = @Schema(implementation = LoanInformationDto.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                    @ApiResponse(responseCode = "403", description = "Forbidden: Insufficient permissions")
            }
    )
    ResponseEntity<Page<LoanInformationDto>> me(Authentication auth, int page, int size);

    @Operation(
            summary = "Approve Loan",
            description = "Allows an employee to approve a loan application by loan number.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully approved loan"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized: Employee authentication failed"),
                    @ApiResponse(responseCode = "404", description = "Loan Not Found - Loan Number not found",
                            content = @Content(schema = @Schema(implementation = LoanNotFound.class))),
            }
    )
    ResponseEntity<Void> approveLoan(@Parameter(description = "Number of the loan") Long loanNumber);

    @Operation(
            summary = "Approve Loan",
            description = "Allows an employee to reject a loan application by loan number.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully rejected loan"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized: Employee authentication failed"),
                    @ApiResponse(responseCode = "404", description = "Loan Not Found - Loan Number not found",
                            content = @Content(schema = @Schema(implementation = LoanNotFound.class))),
            }
    )
    ResponseEntity<Void> rejectLoan(@Parameter(description = "Number of the loan") Long loanNumber);

}
