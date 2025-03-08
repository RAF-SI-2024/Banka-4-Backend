package rs.banka4.user_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.banka4.user_service.dto.RegenerateAuthenticatorResponseDto;
import rs.banka4.user_service.dto.requests.SentCode;
import rs.banka4.user_service.dto.requests.VerificationRequestDto;
import rs.banka4.user_service.service.impl.VerificationEventService;
import rs.banka4.user_service.service.impl.TotpService;

@RestController
@RequestMapping("/verify")
@RequiredArgsConstructor
@Tag(name = "VerifyController", description = "Endpoints for TOTP request verifications")
public class TotpController {

    private final TotpService totpService;
    private final VerificationEventService verificationEventService;

    /**
     * Regenerates a new TOTP secret for the authenticated user.
     *
     * @param auth The authentication object containing the user details.
     * @return A response entity containing the regenerate authenticator response DTO with the new secret.
     */
    @Operation(
            summary = "Regenerate TOTP Secret",
            description = "Regenerates a new TOTP secret for the authenticated user.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "TOTP Secret regenerated successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RegenerateAuthenticatorResponseDto.class)
                            )
                    )
            }
    )
    @GetMapping("/regenerate-authenticator")
    public ResponseEntity<RegenerateAuthenticatorResponseDto> regenerateAuthenticator(
            @Parameter(description = "The authentication object containing user details")
            Authentication auth) {
        return totpService.regenerateSecret(auth);
    }

    /**
     * Verifies the new TOTP code for the user.
     *
     * @param auth The authentication object containing the user details.
     * @param sentCode The code sent by the user for verification.
     * @return A response entity with status 200 if verified, or 404 if not found.
     */
    @Operation(
            summary = "Verify New TOTP Code",
            description = "Verifies the new TOTP code submitted by the user.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Code verified successfully"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Code not verified"
                    )
            }
    )
    @PostMapping("/verify-new-authenticator")
    public ResponseEntity<Void> verifyNewAuthenticator(
            @Parameter(description = "The authentication object containing user details")
            Authentication auth,
            @RequestBody @Valid SentCode sentCode) {
        return totpService.verifyNewAuthenticator(auth, sentCode.content());
    }

    /**
     * Validates the provided TOTP code for the authenticated user.
     *
     * @param sentCode The code sent by the user for validation.
     * @param auth The authentication object containing the user details.
     * @return A response entity with status 200 if the code is valid, or 404 if not found.
     */
    @Operation(
            summary = "Validate TOTP Code",
            description = "Validates the TOTP code submitted by the user for authentication.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Code validated successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(type = "string", example = "Code verified")
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Code not found or invalid"
                    )
            }
    )
    @PostMapping("/validate")
    public ResponseEntity<?> verifyCode(
            @RequestBody @Valid SentCode sentCode,
            @Parameter(description = "The authentication object containing user details")
            Authentication auth) {
        boolean result = totpService.validate((String) auth.getCredentials(), sentCode.content());
        if(result) {
            return ResponseEntity.ok("Code verified");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
