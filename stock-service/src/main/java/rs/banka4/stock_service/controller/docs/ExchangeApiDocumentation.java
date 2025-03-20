package rs.banka4.stock_service.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import rs.banka4.stock_service.domain.exchanges.dtos.ExchangeDto;

public interface ExchangeApiDocumentation {

    @Operation(
        summary = "Get All Exchanges",
        description = "Retrieves all exchanges with pagination. User can see the exchanges"
            + "that are allowed for his role.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved exchanges",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ExchangeDto.class)
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
    ResponseEntity<Page<ExchangeDto>> getAllExchanges(
        @Parameter(description = "Page number") int page,
        @Parameter(description = "Number of exchanges per page") int size
    );
}
