package rs.banka4.stock_service.repositories;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rs.banka4.stock_service.domain.options.db.Option;

@Repository
public interface OptionsRepository extends JpaRepository<Option, UUID> {
    @Query(
        value = "select o from options o where o.stock.id = :stockId and o.settlementDate between :startDate and :endDate"
    )
    List<Option> findAllOptionsForStockWithSettlementDate(
        UUID stockId,
        OffsetDateTime startDate,
        OffsetDateTime endDate
    );
}
