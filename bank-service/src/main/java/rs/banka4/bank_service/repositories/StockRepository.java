package rs.banka4.bank_service.repositories;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.banka4.bank_service.domain.security.stock.db.Stock;

@Repository
public interface StockRepository extends JpaRepository<Stock, UUID> {
    Optional<Stock> findByTicker(String ticker);
}
