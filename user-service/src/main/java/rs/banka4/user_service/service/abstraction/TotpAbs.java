package rs.banka4.user_service.service.abstraction;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import rs.banka4.user_service.dto.RegenerateAuthenticatorResponseDto;

public interface TotpAbs {
    boolean validate(String authorization, String code);
    ResponseEntity<RegenerateAuthenticatorResponseDto> regenerateSecret(Authentication auth);
    ResponseEntity<Void> verifyNewAuthenticator(Authentication auth, String code);
    String generateCode(String authorization);
}
