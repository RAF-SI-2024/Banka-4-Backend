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
import rs.banka4.user_service.dto.ClientContactDto;
import rs.banka4.user_service.dto.ClientDto;
import rs.banka4.user_service.dto.PrivilegesDto;
import rs.banka4.user_service.dto.requests.ClientContactRequest;
import rs.banka4.user_service.dto.requests.UpdateClientDto;
import rs.banka4.user_service.dto.requests.CreateClientDto;

import java.util.UUID;

@Tag(name = "ClientController", description = "Endpoints for clients")
public interface ClientApiDocumentation {

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
    ResponseEntity<PrivilegesDto> getPrivileges(Authentication auth);

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
    ResponseEntity<ClientDto> me(Authentication auth);

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
    ResponseEntity<ClientDto> getClient(@Parameter(description = "ID of the client") UUID id);

    @Operation(
            summary = "Create a new Client",
            description = "Creates a new client with the provided details and a list of account details.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Successfully created new client"),
                    @ApiResponse(responseCode = "400", description = "Bad request - Invalid data or duplicate email")
            }
    )
    ResponseEntity<Void> createClient(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Details of the new client to create", required = true)
            @Valid CreateClientDto createClientDto);

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
    ResponseEntity<Void> updateClient(
            @Parameter(description = "ID of the client") UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Client details for update, including account updates", required = true)
            @Valid UpdateClientDto updateClientDto);

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
    ResponseEntity<Page<ClientDto>> getClients(
            @Parameter(description = "First name of the client") String firstName,
            @Parameter(description = "Last name of the client") String lastName,
            @Parameter(description = "Email address of the client") String email,
            @Parameter(description = "Field to sort by") String sortBy,
            @Parameter(description = "Page number") int page,
            @Parameter(description = "Number of clients per page") int size);

    @Operation(
            summary = "Get Contact",
            description = "Returns client's contacts.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved contacts list",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ClientContactDto.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "No account found")
            }
    )
    ResponseEntity<Page<ClientContactDto>> getAllContacts(
            Authentication authentication,
            @Parameter(description = "Page number") int page,
            @Parameter(description = "Number of clients per page") int size);

    @Operation(
            summary = "Create Contact",
            description = "Creates a new contact",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Successfully created new contact",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ClientContactDto.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "No account found")
            }
    )
    ResponseEntity<Void> createContact(
            Authentication authentication,
            @Valid ClientContactRequest request);

    @Operation(
            summary = "Delete Contact",
            description = "Deletes an existing contact",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully deleted contact",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ClientContactDto.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "No account found")
            }
    )
    ResponseEntity<Void> deleteContact(
            Authentication authentication,
            @Parameter(description = "Account number of the contact to delete") String accountNumber);
}
