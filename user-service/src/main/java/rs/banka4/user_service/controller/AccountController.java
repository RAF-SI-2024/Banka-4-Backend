package rs.banka4.user_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.banka4.user_service.dto.AccountDto;
import rs.banka4.user_service.dto.PaymentDto;
import rs.banka4.user_service.dto.PaymentStatus;
import rs.banka4.user_service.dto.requests.CreateAccountDto;
import rs.banka4.user_service.dto.requests.CreatePaymentDto;
import rs.banka4.user_service.service.abstraction.AccountService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
@Tag(name = "AccountController", description = "Endpoints for accounts")
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/search")
    public ResponseEntity<Page<AccountDto>> getAll(
            Authentication auth,
            @RequestParam(required = false) @Parameter(description = "First name of client") String firstName,
            @RequestParam(required = false) @Parameter(description = "Last name of client") String lastName,
            @RequestParam(required = false) @Parameter(description = "Account id") String id,
            @RequestParam(defaultValue = "0") @Parameter(description = "Page number") int page,
            @RequestParam(defaultValue = "10") @Parameter(description = "Number of employees per page") int size
    ){
        return this.accountService.getAll(firstName, lastName, id, PageRequest.of(page, size));
    }

    @Operation(
            summary = "Get Client Account by ID",
            description = "Retrieves the list of accounts for the authenticated client using a provided account ID. Requires authentication.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved accounts",
                            content = @Content(schema = @Schema(implementation = AccountDto.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Token errors"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Access denied")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccount(@PathVariable String id, Authentication auth) {
        return this.accountService.getAccount(auth.getCredentials().toString(), id);
    }

    @Operation(
            summary = "Get Client Accounts",
            description = "Retrieves the list of accounts for the authenticated client using token. Requires authentication.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved accounts",
                            content = @Content(schema = @Schema(implementation = AccountDto.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Token errors"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Access denied")
            }
    )
    @GetMapping("/")
    public ResponseEntity<List<AccountDto>> getAccountsForClient(Authentication auth) {
        return this.accountService.getAccountsForClient(auth.getCredentials().toString());
    }

    @GetMapping("/recipients")
    public ResponseEntity<List<AccountDto>> getRecentRecipients(Authentication auth) {
        return this.accountService.getRecentRecipientsFor(auth.getCredentials().toString());
    }

    @Operation(
            summary = "Create a new Account",
            description = "Creates a new account with the provided details.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Successfully created new account"),
                    @ApiResponse(responseCode = "400", description = "Bad request - Invalid data")
            }
    )
    @PostMapping
    public ResponseEntity<Void> createAccount(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Details of the new account to create", required = true)
            @RequestBody @Valid CreateAccountDto createAccountDto) {
        return accountService.createAccount(createAccountDto);
    }
}