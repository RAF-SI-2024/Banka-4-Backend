package rs.banka4.stock_service.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import rs.banka4.stock_service.domain.assets.db.AssetOwnership;
import rs.banka4.stock_service.domain.assets.dtos.TransferDto;

public interface StocksApiDocumentation {

    @Operation(
        summary = "Transfer amount od some stocks to PRIVATE or PUBLIC",
        description = "Transfers stocks of user from PRIVATE to PUBLIC or vice versa depending on transferTo enum value",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully transferred data",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AssetOwnership.class)
                )
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Not enough stocks to transfer (NotEnoughStock)"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Row for user stocks not found, or id given is not for stock (StockOwnershipNotFound)"
            )
        }
    )
    ResponseEntity<AssetOwnership> transferStocks(
        Authentication auth,
        @RequestBody TransferDto transferDto
    );
}
