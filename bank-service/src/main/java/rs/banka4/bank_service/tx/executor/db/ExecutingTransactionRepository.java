package rs.banka4.bank_service.tx.executor.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;

@Repository
public interface ExecutingTransactionRepository extends
    JpaRepository<ExecutingTransaction, ForeignBankId> {
}
