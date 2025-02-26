package rs.banka4.user_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.banka4.user_service.models.VerificationCode;

import java.util.List;
import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    Optional<VerificationCode> findByCode(String code);
    Optional<VerificationCode> findByEmail(String email);
}
