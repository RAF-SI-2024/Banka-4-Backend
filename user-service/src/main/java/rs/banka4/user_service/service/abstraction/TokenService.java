package rs.banka4.user_service.service.abstraction;

import rs.banka4.user_service.models.Token;

import java.util.Optional;

public interface TokenService {
    void invalidateToken(String token);
    Optional<Token> findByToken(String token);
}
