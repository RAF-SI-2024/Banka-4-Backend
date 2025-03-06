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
import rs.banka4.user_service.dto.ClientContactDto;
import rs.banka4.user_service.dto.ClientDto;
import rs.banka4.user_service.dto.PrivilegesDto;
import rs.banka4.user_service.dto.requests.ClientContactRequest;
import rs.banka4.user_service.dto.requests.UpdateClientDto;
import rs.banka4.user_service.service.abstraction.ClientService;
import rs.banka4.user_service.dto.requests.CreateClientDto;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/client")
@RequiredArgsConstructor
@Tag(name = "ClientController", description = "Endpoints for clients")
public class ClientController {

    private final ClientService clientService;

    @Operation(
            summary = "Get Client Privileges",
            description = "Retrieves the list of privileges for the authenticated client. Requires authentication.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved privileges",
                            content = @Content(schema = @Schema(implementation = PrivilegesDto.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Token errors"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Access denied")
            }
    )
    @GetMapping("/privileges")
    public ResponseEntity<PrivilegesDto> getPrivileges(Authentication auth) {
        PrivilegesDto privilegesDto = new PrivilegesDto(List.of());
        return ResponseEntity.ok(privilegesDto);
    }

    @Operation(
            summary = "Get Client Information",
            description = "Retrieves information about the authenticated client using the token provided in the Authorization header.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved client details",
                            content = @Content(schema = @Schema(implementation = ClientDto.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Access denied due to invalid token")
            }
    )
    @GetMapping("/me")
    public ResponseEntity<ClientDto> me(Authentication auth) {
        return clientService.getMe(auth.getCredentials().toString());
    }

    @Operation(
            summary = "Get Client Information by ID",
            description = "Retrieves client information by client ID. Admin privileges required.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved client details",
                            content = @Content(schema = @Schema(implementation = ClientDto.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Admin privileges required"),
                    @ApiResponse(responseCode = "404", description = "Not Found - Client id not found")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<ClientDto> getClient(@PathVariable UUID id) {
        return clientService.getClient(String.valueOf(id));
    }

    @Operation(
            summary = "Create a new Client",
            description = "Creates a new client with the provided details and a list of account details.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Successfully created new client"),
                    @ApiResponse(responseCode = "400", description = "Bad request - Invalid data or duplicate email")
            }
    )
    @PostMapping
    public ResponseEntity<Void> createClient(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Details of the new client to create", required = true)
            @RequestBody @Valid CreateClientDto createClientDto) {
        return clientService.createClient(createClientDto);
    }

    @Operation(
            summary = "Update Client",
            description = "Allows a client or admin to update the details of a client, including a list of account updates.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully updated client"),
                    @ApiResponse(responseCode = "400", description = "Invalid data provided"),
                    @ApiResponse(responseCode = "409", description = "Duplicate email or username"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Access denied")
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateClient(
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Client details for update, including account updates", required = true)
            @RequestBody @Valid UpdateClientDto updateClientDto) {
        return clientService.updateClient(String.valueOf(id), updateClientDto);
    }

    @Operation(
            summary = "Search Clients",
            description = "Searches for clients based on the provided filters. Admin access required.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved clients list",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ClientDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid data for search filters"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Admin privileges required")
            }
    )
    @GetMapping("/search")
    public ResponseEntity<Page<ClientDto>> getClients(
            @RequestParam(required = false) @Parameter(description = "First name of the client") String firstName,
            @RequestParam(required = false) @Parameter(description = "Last name of the client") String lastName,
            @RequestParam(required = false) @Parameter(description = "Email address of the client") String email,
            @RequestParam(required = false) @Parameter(description = "Field to sort by") String sortBy,
            @RequestParam(defaultValue = "0") @Parameter(description = "Page number") int page,
            @RequestParam(defaultValue = "10") @Parameter(description = "Number of clients per page") int size) {
        return clientService.getAll(firstName, lastName, email, sortBy, PageRequest.of(page, size));
    }

    @Operation(
            summary = "Get Contact",
            description = "Returns client's contacts.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved contacts list",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ClientContactDto.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "No account found"),
            }
    )
    @GetMapping("/contacts")
    public ResponseEntity<Page<ClientContactDto>> getAllContacts(
            Authentication authentication,
            @RequestParam(defaultValue = "0") @Parameter(description = "Page number") int page,
            @RequestParam(defaultValue = "10") @Parameter(description = "Number of clients per page") int size
    ) {
        return clientService.getAllContacts(authentication.getCredentials().toString(), PageRequest.of(page, size));
    }

    @Operation(
            summary = "Create Contact",
            description = "Creates a new contact",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Successfully created new contact",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ClientContactDto.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "No account found"),
            }
    )
    @PostMapping("/create-contact")
    public ResponseEntity<Void> createContact(Authentication authentication, @RequestBody @Valid ClientContactRequest request) {
        return clientService.createContact(authentication.getCredentials().toString(), request);
    }

    @Operation(
            summary = "Delete Contact",
            description = "Deletes an existing contact",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully deleted contact",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ClientContactDto.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "No account found"),
            }
    )
    @DeleteMapping("/delete-contact")
    public ResponseEntity<Void> deleteContact(Authentication authentication, @RequestBody String accountNumber) {
        return clientService.deleteContact(authentication.getCredentials().toString(), accountNumber);
    }
}
