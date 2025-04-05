package rs.banka4.stock_service.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.banka4.stock_service.domain.actuaries.db.dto.ActuaryPayloadDto;
import rs.banka4.stock_service.domain.listing.dtos.ListingDto;
import rs.banka4.stock_service.domain.response.CombinedResponse;

public interface ActuaryApiDocumentation {

    @Operation(
        summary = "Register new actuary",
        description = "Upon creation of a new employee with actuary status, this creates a new actuary,"
            + "which shares its ID with the ID of the employee",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully added actuary",
                content = @Content(
                    // mediaType = "application/json", schema = @Schema(implementation = )
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid request parameters"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden"
            )
        }
    )
    ResponseEntity<?> register(@RequestBody @Valid ActuaryPayloadDto dto);


    @Operation(
        summary = "Update actuary",
        description = "Upon update of an employee with actuary status, this updates the actuaries contents "
            + "i.e. the limits and needed approval",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully updated actuary",
                content = @Content(
                    // mediaType = "application/json", schema = @Schema(implementation = )
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid request parameters"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Actuary not found"
            ),
        }
    )
    ResponseEntity<?> update(@PathVariable UUID id, @RequestBody @Valid ActuaryPayloadDto dto);


    @Operation(
        summary = "Search agents",
        description = """
            Searches and filters employees with the AGENT role based on provided filters:
            first name, last name, email, and position.

            This endpoint is accessible only to users with ADMIN or SUPERVISOR privileges.
            """,
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved agent listings",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ListingDto.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid request parameters"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - user does not have ADMIN or SUPERVISOR privilege"
            )
        }
    )
    @PostMapping("/search")
    ResponseEntity<Page<CombinedResponse>> search(
        @RequestParam(required = false) String firstName,
        @RequestParam(required = false) String lastName,
        @RequestParam(required = false) String email,
        @RequestParam(required = false) String position,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        Authentication auth
    );

    @Operation(
        summary = "Reset limit for actuary",
        description = "This endpoint allows a SUPERVISOR to reset the limit for an agent to 0.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully reset the limit"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - Only supervisors can reset the limit"
            )
        }
    )
    @PutMapping("/limit/reset/{actuaryId}")
    ResponseEntity<Void> resetUsedLimit(@PathVariable UUID actuaryId);
}
