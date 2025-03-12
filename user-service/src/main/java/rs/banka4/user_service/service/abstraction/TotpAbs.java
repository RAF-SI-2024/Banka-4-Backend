package rs.banka4.user_service.service.abstraction;

import org.springframework.security.core.Authentication;
import rs.banka4.user_service.domain.authenticator.dtos.RegenerateAuthenticatorResponseDto;

public interface TotpAbs {
    boolean validate(String authorization, String code);

    RegenerateAuthenticatorResponseDto regenerateSecret(Authentication auth);

    void verifyNewAuthenticator(Authentication auth, String code);

    String generateCode(String authorization);
}
