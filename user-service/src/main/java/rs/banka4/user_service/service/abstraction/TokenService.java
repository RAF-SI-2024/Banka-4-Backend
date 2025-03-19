package rs.banka4.user_service.service.abstraction;

import java.util.Optional;
import rs.banka4.user_service.domain.auth.db.Token;

public interface TokenService {
    void invalidateToken(String token);

    Optional<Token> findByToken(String token);
}
