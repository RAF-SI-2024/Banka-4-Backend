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
import org.springframework.web.bind.annotation.RequestParam;
import rs.banka4.user_service.domain.loan.db.LoanStatus;
import rs.banka4.user_service.domain.loan.db.LoanType;
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
    ResponseEntity<Void> createLoanApplication(LoanApplicationDto loanApplicationDto, Authentication authentication);

    @Operation(
            summary = "Search All Loans",
            description = "Allows an employee to search and retrieve all loans in the system. " +
                    "This endpoint supports pagination and filtering by loan type, status, and account number. " +
                    "Use the 'page' and 'size' parameters to control pagination. " +
                    "The 'type' and 'status' parameters are optional and should match the values defined in the corresponding enums for LoanType and LoanStatus. " +
                    "If an invalid loan type or status is provided, a 404 Not Found error is returned. " +
                    "By default, if the loan status is PROCESSING, the results are sorted by agreement date in descending order; " +
                    "otherwise, the results are sorted by account number in ascending order. " +
                    "The 'accountNumber' parameter allows filtering of loans associated with a specific account.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Loans retrieved successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoanInformationDto.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized: Employee authentication failed", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: Insufficient permissions", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Not Found: Incorrect loan type or status provided", content = @Content)
            }
    )
    ResponseEntity<Page<LoanInformationDto>> getAllLoans(int page, int size, String type, String status, String accountNumber);

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
    ResponseEntity<Void> approveLoan(@Parameter(description = "Number of the loan") Long loanNumber, Authentication auth);

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
    ResponseEntity<Void> rejectLoan(@Parameter(description = "Number of the loan") Long loanNumber, Authentication auth);

}
