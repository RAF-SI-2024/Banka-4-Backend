package rs.banka4.bank_service.tx.executor.db;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rs.banka4.bank_service.tx.data.IdempotenceKey;

@Repository
public interface InboxRepository extends JpaRepository<InboxMessage, IdempotenceKey> {
    @Query("SELECT im FROM InboxMessage im WHERE im.id = :id")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<InboxMessage> findAndLock(IdempotenceKey id);
}
