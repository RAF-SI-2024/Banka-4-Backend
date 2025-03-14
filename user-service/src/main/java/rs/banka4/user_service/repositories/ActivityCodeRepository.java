package rs.banka4.user_service.repositories;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.banka4.user_service.domain.company.db.ActivityCode;

@Repository
public interface ActivityCodeRepository extends JpaRepository<ActivityCode, UUID> {
    Optional<ActivityCode> findActivityCodeByCode(String activityCode);

    boolean existsByCode(String code);
}
