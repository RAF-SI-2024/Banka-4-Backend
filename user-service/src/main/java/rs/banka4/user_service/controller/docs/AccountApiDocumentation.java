package rs.banka4.user_service.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import rs.banka4.user_service.domain.account.dtos.AccountDto;
import rs.banka4.user_service.domain.account.dtos.CreateAccountDto;
import rs.banka4.user_service.exceptions.user.client.ClientNotFound;
import rs.banka4.user_service.exceptions.company.CompanyNotFound;
import rs.banka4.user_service.exceptions.user.employee.EmployeeNotFound;
import rs.banka4.user_service.exceptions.account.InvalidCurrency;

import java.util.Set;

@Tag(name = "AccountController", description = "Endpoints for accounts")
public interface AccountApiDocumentation {

    @Operation(
            summary = "Search for accounts",
            description = "Search for accounts based on client information such as first name, last name, account number. Supports pagination.",
            security = @SecurityRequirement(name = "bearerAuth"),
            parameters = {
                    @Parameter(name = "firstName", description = "First name of the client"),
                    @Parameter(name = "lastName", description = "Last name of the client"),
                    @Parameter(name = "accountNumber", description = "Account number"),
                    @Parameter(name = "page", description = "Page number for pagination", schema = @Schema(defaultValue = "0")),
                    @Parameter(name = "size", description = "Number of accounts per page", schema = @Schema(defaultValue = "10"))
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved the list of accounts",
                            content = @Content(schema = @Schema(implementation = AccountDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Invalid authentication token"
                    )
            }
    )
    ResponseEntity<Page<AccountDto>> getAll(Authentication auth,
                                            String firstName,
                                            String lastName,
                                            String accountNumber,
                                            int page,
                                            int size);

    @Operation(
            summary = "Get Client Account by account number",
            description = "Retrieves the account details for a given account ID. Requires authentication.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved account",
                            content = @Content(schema = @Schema(implementation = AccountDto.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Token errors"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Access denied")
            }
    )
    ResponseEntity<AccountDto> getAccount(String id, Authentication auth);

    @Operation(
            summary = "Get Client Accounts",
            description = "Retrieves the list of accounts for the authenticated client. Requires authentication.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved accounts",
                            content = @Content(schema = @Schema(implementation = AccountDto.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Token errors"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Access denied"),
                    @ApiResponse(responseCode = "404", description = "Client not found",
                            content = @Content(schema = @Schema(implementation = ClientNotFound.class)))
            }
    )
    ResponseEntity<Set<AccountDto>> getAccountsForClient(Authentication auth);

    @Operation(
            summary = "Create a new Account with optional card",
            description = "Creates a new banking account. For business accounts, automatically generates " +
                    "a card with client as authorized user if requested. Personal accounts can have " +
                    "up to 2 cards, business accounts 1 per authorized user.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Successfully created new account"),
                    @ApiResponse(responseCode = "400", description = "Invalid currency",
                            content = @Content(schema = @Schema(implementation = InvalidCurrency.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request - Invalid data"),
                    @ApiResponse(responseCode = "404", description = "Client not found",
                            content = @Content(schema = @Schema(implementation = ClientNotFound.class))),
                    @ApiResponse(responseCode = "404", description = "Company not found",
                            content = @Content(schema = @Schema(implementation = CompanyNotFound.class))),
                    @ApiResponse(responseCode = "404", description = "Employee not found",
                            content = @Content(schema = @Schema(implementation = EmployeeNotFound.class))),
                    @ApiResponse(responseCode = "409", description = "Card limit exceeded")
            }
    )
    ResponseEntity<Void> createAccount(@Valid CreateAccountDto createAccountDto, Authentication auth);
}
