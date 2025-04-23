package rs.banka4.bank_service.tx.executor.db;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxMessage, ForeignBankId> {
    @Query("SELECT o FROM OutboxMessage o WHERE o.lastSendTime < :lastInstant AND NOT o.delivered")
    List<OutboxMessage> findAllSentBefore(Instant lastInstant);
}
