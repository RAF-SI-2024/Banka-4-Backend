package rs.banka4.bank_service.tx.executor.db;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;
import rs.banka4.bank_service.domain.transaction.db.TransactionStatus;

@Repository
public interface ExecutingTransactionRepository extends
    JpaRepository<ExecutingTransaction, ForeignBankId> {
    @Query("SELECT o FROM ExecutingTransaction o WHERE o.id = :id")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ExecutingTransaction> findAndLockTx(ForeignBankId id);

    @Modifying
    @Query("""
        UPDATE Transaction t
               SET t.status =
                     CASE (SELECT et.votesAreYes
                                  FROM ExecutingTransaction et
                                  WHERE et.id = t.executingTransaction)
                          WHEN TRUE THEN 'REALIZED'
                          ELSE 'REJECTED'
                     END
            WHERE EXISTS (SELECT 1 FROM ExecutingTransaction et
                                   WHERE et.id = t.executingTransaction
                                         AND et.votesCast = et.neededVotes)
        """)
    void updateStaleTxStatuses();

    /**
     * Sets the status of each transaction whose executing transaction is ID'd @{code execTx} to the
     * provided transaction status.
     *
     * @see TxExecutor for an explanation on why
     */
    @Query("""
        UPDATE Transaction t SET t.status = :newTxStatus WHERE t.executingTransaction = :execTx
        """)
    @Modifying
    void setTransactionStatusForExecutingTransactionConstituents(
        ForeignBankId execTx,
        TransactionStatus newTxStatus
    );
}
