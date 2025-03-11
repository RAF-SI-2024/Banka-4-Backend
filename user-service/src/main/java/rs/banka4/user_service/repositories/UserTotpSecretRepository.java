package rs.banka4.user_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.banka4.user_service.domain.authenticator.db.UserTotpSecret;

import java.util.Optional;
import java.util.UUID;

public interface UserTotpSecretRepository extends JpaRepository<UserTotpSecret, UUID> {
    Optional<UserTotpSecret> findByClient_Email(String email);
    Optional<UserTotpSecret> findByEmployee_Email(String email);
    Optional<UserTotpSecret> findByClient_Id(UUID clientId);
    boolean existsByClient_Email(String email);
    boolean existsByEmployee_Email(String email);
}
