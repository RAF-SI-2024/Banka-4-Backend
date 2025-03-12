package rs.banka4.user_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.banka4.user_service.controller.docs.TotpControllerDocumentation;
import rs.banka4.user_service.domain.authenticator.db.SentCode;
import rs.banka4.user_service.domain.authenticator.dtos.RegenerateAuthenticatorResponseDto;
import rs.banka4.user_service.service.impl.TotpService;

@RestController
@RequestMapping("/verify")
@RequiredArgsConstructor
public class TotpController implements TotpControllerDocumentation {

    private final TotpService totpService;

    @Override
    @GetMapping("/regenerate-authenticator")
    public ResponseEntity<RegenerateAuthenticatorResponseDto> regenerateAuthenticator(
        Authentication auth
    ) {
        return ResponseEntity.ok(totpService.regenerateSecret(auth));
    }

    @Override
    @PostMapping("/verify-new-authenticator")
    public ResponseEntity<Void> verifyNewAuthenticator(
        Authentication auth,
        @RequestBody @Valid SentCode sentCode
    ) {
        totpService.verifyNewAuthenticator(auth, sentCode.content());
        return ResponseEntity.ok()
            .build();
    }

    @Override
    @PostMapping("/validate")
    public ResponseEntity<?> verifyCode(
        @RequestBody @Valid SentCode sentCode,
        Authentication auth
    ) {
        boolean result = totpService.validate((String) auth.getCredentials(), sentCode.content());
        if (result) {
            return ResponseEntity.ok("Code verified");
        } else {
            return ResponseEntity.notFound()
                .build();
        }
    }
}
