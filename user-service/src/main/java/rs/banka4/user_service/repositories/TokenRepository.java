package rs.banka4.user_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.banka4.user_service.models.Token;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, String> {
    Optional<Token> findByToken(String token);
}
