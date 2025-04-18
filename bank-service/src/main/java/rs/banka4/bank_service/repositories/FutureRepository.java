package rs.banka4.bank_service.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.banka4.bank_service.domain.security.future.db.Future;

@Repository
public interface FutureRepository extends JpaRepository<Future, UUID> {
}
