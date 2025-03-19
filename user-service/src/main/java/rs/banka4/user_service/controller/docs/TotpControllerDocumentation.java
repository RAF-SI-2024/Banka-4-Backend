package rs.banka4.user_service.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import rs.banka4.user_service.domain.authenticator.db.SentCode;
import rs.banka4.user_service.domain.authenticator.dtos.RegenerateAuthenticatorResponseDto;

@Tag(
    name = "VerifyController",
    description = "Endpoints for TOTP request verifications"
)
public interface TotpControllerDocumentation {

    @Operation(
        summary = "Regenerate TOTP Secret",
        description = "Regenerates a new TOTP secret for the authenticated user.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "TOTP Secret regenerated successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RegenerateAuthenticatorResponseDto.class)
                )
            )
        }
    )
    ResponseEntity<RegenerateAuthenticatorResponseDto> regenerateAuthenticator(
        @Parameter(
            description = "The authentication object containing user details"
        ) Authentication auth
    );

    @Operation(
        summary = "Verify New TOTP Code",
        description = "Verifies the new TOTP code submitted by the user.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Code verified successfully"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Code not verified"
            )
        },
        requestBody = @RequestBody(
            description = "The code sent by the user for verification",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SentCode.class)
            )
        )
    )
    ResponseEntity<Void> verifyNewAuthenticator(
        @Parameter(
            description = "The authentication object containing user details"
        ) Authentication auth,
        SentCode sentCode
    );

    @Operation(
        summary = "Validate TOTP Code",
        description = "Validates the TOTP code submitted by the user for authentication.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Code validated successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                        type = "string",
                        example = "Code verified"
                    )
                )
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Code not found or invalid"
            )
        },
        requestBody = @RequestBody(
            description = "The code to validate",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SentCode.class)
            )
        )
    )
    ResponseEntity<?> verifyCode(
        SentCode sentCode,
        @Parameter(
            description = "The authentication object containing user details"
        ) Authentication auth
    );
}
