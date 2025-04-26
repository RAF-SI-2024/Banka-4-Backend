package rs.banka4.bank_service.tx;

import rs.banka4.bank_service.domain.trading.db.ForeignBankId;
import rs.banka4.bank_service.domain.transaction.db.Transaction;
import rs.banka4.bank_service.domain.transaction.db.TransactionStatus;
import rs.banka4.bank_service.tx.data.DoubleEntryTransaction;

/**
 * Given a 2EA formatted transaction (see section 2.8. Transactions
 * <a href="https://arsen.srht.site/si-tx-proto/">here</a>), submit it to the execution queue.
 *
 * <p>
 * Submitting a transaction involves writing it down into a database first, so that it may be
 * executed upon recovery in case of a crash. The executor shall only succeed if the transaction was
 * written down.
 *
 * <p>
 * The transaction system will also update the {@link Transaction} table, setting all
 * {@link Transaction#setStatus(TransactionStatus)} to {@link TransactionStatus#REALIZED} upon
 * commit, and {@link TransactionStatus#REJECTED} upon rollback.
 */
public interface TxExecutor {
    /**
     * Submits a transaction for asynchronous execution. Returns successfully if and only if
     * {@code txDesc} was persisted and will happen <i>eventually</i>.
     *
     * <p>
     * <strong>Note that this function cannot be rolled back</strong>! If a transaction is
     * submitted, and this function returns successfully, it <strong>will</strong> happen eventually
     * (the transaction may fail asynchronously). Calls to this function also do not affect the
     * currently-active transaction (i.e. a failure of {@code submitTx} does not fail the outer
     * transaction, and an outer rollback does not roll back {@code submitTx}).
     *
     * @param txDesc Transaction to eventually execute. Its ID will be overwritten.
     * @return The ID of this transaction.
     */
    ForeignBankId submitTx(DoubleEntryTransaction txDesc);

    /**
     * Executes a transaction that is fully local synchronously. Unlike
     * {@link #submitTx(DoubleEntryTransaction)}, the (database) transaction used is the callers'
     * transaction, so rolling back will, in fact, roll back the effects of this function.
     *
     * @param txDesc Transaction to immediately execute. Its ID will be overwritten.
     * @return The ID of this transaction.
     */
    ForeignBankId submitImmediateTx(DoubleEntryTransaction txDesc);
}
