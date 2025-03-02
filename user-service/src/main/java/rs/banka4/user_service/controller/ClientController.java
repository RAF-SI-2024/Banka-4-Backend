package rs.banka4.user_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.banka4.user_service.dto.ClientDto;
import rs.banka4.user_service.dto.EmployeeResponseDto;
import rs.banka4.user_service.service.impl.ClientServiceImpl;

@RestController
@RequestMapping("/client")
@RequiredArgsConstructor
@Tag(name = "ClientController", description = "Endpoints for clients")
public class ClientController {
    private final ClientServiceImpl clientService;




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
        return clientService.getMe((String) auth.getCredentials());
    }
}
