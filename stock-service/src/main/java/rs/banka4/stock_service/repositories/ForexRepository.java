package rs.banka4.stock_service.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.banka4.stock_service.domain.security.forex.db.ForexPair;

@Repository
public interface ForexRepository extends JpaRepository<ForexPair, UUID> {
}
