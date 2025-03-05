package rs.banka4.user_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.banka4.user_service.models.UserTotpSecret;

import java.util.Optional;

public interface UserTotpSecretRepository extends JpaRepository<UserTotpSecret, Long> {
    Optional<UserTotpSecret> findByClient_Email(String email);
    Optional<UserTotpSecret> findByEmployee_Email(String email);
    boolean existsByClient_Email(String email);
    boolean existsByEmployee_Email(String email);
}
