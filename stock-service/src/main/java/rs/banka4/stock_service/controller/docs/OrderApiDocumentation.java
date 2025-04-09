package rs.banka4.stock_service.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import rs.banka4.stock_service.domain.orders.dtos.CreateOrderDto;
import rs.banka4.stock_service.domain.orders.dtos.CreateOrderPreviewDto;
import rs.banka4.stock_service.domain.orders.dtos.OrderDto;
import rs.banka4.stock_service.domain.orders.dtos.OrderPreviewDto;

public interface OrderApiDocumentation {

    @Operation(
        summary = "Create an order (buy/sell request)",
        description = """
            Creates a new trading order for a specific financial instrument.
            The order can be of type BUY or SELL, depending on the direction.

            If the order exceeds the trader's daily limit, or if approval is required,
            it will need to be reviewed by a supervisor.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Order successfully created",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = OrderDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content
        )
    })
    ResponseEntity<OrderDto> createOrder(
        @RequestBody(
            description = "Payload containing order creation details",
            required = true,
            content = @Content(schema = @Schema(implementation = CreateOrderDto.class))
        ) @Valid CreateOrderDto request,
        Authentication auth
    );

    @Operation(
        summary = "Calculate average price for an order preview",
        description = """
            Calculates the average price for a given order preview request.
            This can be used to estimate the cost or proceeds of an order before it is placed.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Average price successfully calculated",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = OrderPreviewDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content
        )
    })
    ResponseEntity<OrderPreviewDto> calculateAveragePrice(
        @RequestBody(
            description = "Payload containing order preview details",
            required = true,
            content = @Content(schema = @Schema(implementation = CreateOrderPreviewDto.class))
        ) @Valid CreateOrderPreviewDto request,
        Authentication auth
    );
}
