package rs.banka4.bank_service.tx.executor;

import static java.util.function.Predicate.*;
import static rs.banka4.bank_service.tx.TxUtils.isTxBalanced;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;
import rs.banka4.bank_service.repositories.AccountRepository;
import rs.banka4.bank_service.tx.TxExecutor;
import rs.banka4.bank_service.tx.TxUtils;
import rs.banka4.bank_service.tx.config.InterbankConfig;
import rs.banka4.bank_service.tx.data.IdempotenceKey;
import rs.banka4.bank_service.tx.data.Message;
import rs.banka4.bank_service.tx.data.MonetaryAsset;
import rs.banka4.bank_service.tx.data.NoVoteReason;
import rs.banka4.bank_service.tx.data.Transaction;
import rs.banka4.bank_service.tx.data.TxAccount;
import rs.banka4.bank_service.tx.data.TxAsset;
import rs.banka4.bank_service.tx.errors.MessagePrepFailedException;
import rs.banka4.bank_service.tx.errors.TxLocalPartVotedNo;
import rs.banka4.bank_service.tx.executor.db.ExecutingTransaction;
import rs.banka4.bank_service.tx.executor.db.ExecutingTransactionRepository;
import rs.banka4.bank_service.tx.executor.db.OutboxMessage;
import rs.banka4.bank_service.tx.executor.db.OutboxRepository;

/**
 * A transaction executor capable of delivering transactions across banks.
 */
@Service
@Slf4j
public class InterbankTxExecutor implements TxExecutor, ApplicationRunner {
    private final InterbankConfig interbankConfig;
    private final TransactionTemplate txTemplate;
    private final ObjectMapper objectMapper;
    private final AccountRepository accountRepo;
    private final OutboxRepository outboxRepo;
    private final ExecutingTransactionRepository execTxRepo;
    private final TaskScheduler taskScheduler;
    private final EntityManager entityManager;

    /* Synchronization key for transaction execution. */
    private final Object transactionKey = new Object();

    public InterbankTxExecutor(
        InterbankConfig config,
        PlatformTransactionManager transactionManager,
        ObjectMapper objectMapper,
        AccountRepository accountRepo,
        OutboxRepository outboxRepo,
        ExecutingTransactionRepository execTxRepo,
        TaskScheduler taskScheduler,
        EntityManager entityManager
    ) {
        this.interbankConfig = config;
        this.txTemplate = new TransactionTemplate(transactionManager);
        this.txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.txTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
        this.objectMapper = objectMapper;
        this.accountRepo = accountRepo;
        this.outboxRepo = outboxRepo;
        this.execTxRepo = execTxRepo;
        this.taskScheduler = taskScheduler;
        this.entityManager = entityManager;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    private final IdempotenceKey newIdempotenceKey() {
        final var key =
            new IdempotenceKey(
                ForeignBankId.OUR_ROUTING_NUMBER,
                UUID.randomUUID()
                    .toString()
            );

        /* TODO(arsen): detect reuse */
        return key;
    }

    private Set<Long> collectAndValidateDestinations(Transaction tx) {
        final var dests = TxUtils.collectDestinations(tx);
        final var validDests =
            interbankConfig.getRoutingTable()
                .keySet();
        if (!validDests.containsAll(dests))
            throw new IllegalArgumentException(
                "Destination(s) %s are invalid".formatted(
                    dests.stream()
                        .filter(not(validDests::contains))
                        .toList()
                )
            );
        return dests;
    }

    /**
     * Perform phase one of local transaction execution:
     *
     * <blockquote> Firstly, upon receiving a transaction from an Initiating Bank (IB), a
     * transaction is <i>prepared</i>: all credited accounts have the transacted amount of assets
     * <i>reserved</i>. If this was not possible (for instance, because an account would be
     * overdrafted), the transaction fails locally, the failure is recorded, and a NO vote is cast
     * (voting is described later). Otherwise, a YES vote is cast. </blockquote>
     *
     * <p>
     * Caller should hold {@link #transactionKey}.
     *
     * @returns A list of reasons not to accept a transaction. The caller is expected to make the
     *          transaction roll back if the list is non-empty.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    private List<NoVoteReason> executeLocalPhase1(Transaction tx) {
        if (!isTxBalanced(tx)) return List.of(new NoVoteReason.UnbalancedTx());

        final var noReasons = new ArrayList<NoVoteReason>();
        for (final var posting : tx.postings()) {
            if (
                posting.account()
                    .routingNumber()
                    != ForeignBankId.OUR_ROUTING_NUMBER
            ) continue;

            switch (posting.account()) {
            case TxAccount.Person(ForeignBankId personId) -> throw new NotImplementedException();
            case TxAccount.Option(ForeignBankId optionId) -> throw new NotImplementedException();

            case TxAccount.Account(String accNumber) -> {
                final var accMby = accountRepo.findAccountByAccountNumber(accNumber);
                if (!accMby.isPresent()) {
                    noReasons.add(new NoVoteReason.NoSuchAccount(posting));
                    continue;
                }
                final var acc = accMby.get();
                entityManager.lock(acc, LockModeType.PESSIMISTIC_WRITE);
                entityManager.refresh(acc);

                if (
                    /* Monetary assets are the only kind depositable to accounts. */
                    !(posting.asset() instanceof TxAsset.Monas(MonetaryAsset asset))
                        /* ... but their currencies must match. */
                        || !asset.currency()
                            .equals(acc.getCurrency())
                ) {
                    noReasons.add(new NoVoteReason.UnacceptableAsset(posting));
                    continue;
                }

                /* See Note [Phase-by-phase balance changes]. */
                final var newAvBalance =
                    acc.getAvailableBalance()
                        .add(
                            posting.amount()
                                .min(BigDecimal.ZERO)
                        );

                if (newAvBalance.signum() < 0) {
                    noReasons.add(new NoVoteReason.InsufficientAsset(posting));
                    continue;
                }

                acc.setAvailableBalance(newAvBalance);
                accountRepo.save(acc);
            }
            }
        }

        return noReasons;
    }

    /**
     * Perform phase two of local transaction execution:
     *
     * <blockquote> Should all involved parties vote YES, the IB will send, to all involved parties,
     * a <i>commit</i> message. Upon receiving a commit message, the bank shall erase previously
     * reserved resources, and <i>debit</i> any new assets. </blockquote>
     *
     * <p>
     * Note that {@code tx} <strong>must be valid</strong>. No validation will be performed.
     *
     * <p>
     * Caller should hold {@link #transactionKey}.
     *
     * @returns A list of reasons not to accept a transaction. The caller is expected to make the
     *          transaction roll back if the list is non-empty.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    private void executeLocalPhase2(Transaction tx) {
        for (final var posting : tx.postings()) {
            if (
                posting.account()
                    .routingNumber()
                    != ForeignBankId.OUR_ROUTING_NUMBER
            ) continue;

            switch (posting.account()) {
            case TxAccount.Person(ForeignBankId personId) -> throw new NotImplementedException();
            case TxAccount.Option(ForeignBankId optionId) -> throw new NotImplementedException();

            case TxAccount.Account(String accNumber) -> {
                final var acc =
                    accountRepo.findAccountByAccountNumber(accNumber)
                        .orElseThrow(() -> new IllegalStateException("Invalid tx?"));
                entityManager.lock(acc, LockModeType.PESSIMISTIC_WRITE);
                entityManager.refresh(acc);

                /* @formatter:off
                 * Note [Phase-by-phase balance changes]
                 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                 *
                 * Let A be the available balance, B the (total) balance, and X be the amount of the
                 * posting.  If X is negative, the posting is a credit, otherwise, it is a debit.
                 * Let A_r and B_r be the resulting available and total balance.  Let A' and B' be
                 * the available and total balance at the start of phase two (commit).
                 *
                 * After a successful transaction, A_r = A + X, B_r = B + X.  Case by case, at the
                 * end of phase one:
                 *
                 * |        | A'    | B' |
                 * |--------+-------+----|
                 * | Debit  | A     | B  |
                 * | Credit | A + X | B  |
                 *
                 * Notice, however, that in the debit case, A' = A = A + \min(0, X) (since X >= 0).
                 * In the credit case, \min(0, X) = X (since X < 0), so, both cases can be boiled
                 * down to A' = A + \min(0, X).
                 *
                 * Hence, the result of phase one is A' = A + \min(0, X), B' = B.
                 *
                 * Phase two needs to finish the transaction, and make A_r = A + X and B_r = B + X.
                 * Given that A = A' - \min(0, X), it follows that A_r = A' - \min(0, X) + X.
                 *
                 * Case by case:
                 *
                 * | X <=> 0 | -\min(0, X) + X             |
                 * |---------+-----------------------------|
                 * | X < 0   | -X + X = 0                  |
                 * | X >= 0  | -0 + X = X                  |
                 * | always  | -\min(0, X) + X = max(0, X) |
                 *
                 * Hence, A_r = A' + max(0, X).
                 *
                 * With a much simpler derivation, B_r = B' + X.
                 * @formatter:on
                 */

                acc.setBalance(
                    acc.getBalance()
                        .add(posting.amount())
                );
                acc.setAvailableBalance(
                    acc.getAvailableBalance()
                        .add(
                            posting.amount()
                                .max(BigDecimal.ZERO)
                        )
                );
                accountRepo.save(acc);
            }
            }
        }
    }

    /** Precondition: {@code tx} was voted YES locally. */
    @Transactional(propagation = Propagation.MANDATORY)
    private ExecutingTransaction recordTx(Transaction tx, int destinationCount) {
        final String txAsString;
        try {
            txAsString = objectMapper.writeValueAsString(tx);
        } catch (JsonProcessingException e) {
            throw new MessagePrepFailedException(e);
        }

        return execTxRepo.save(
            new ExecutingTransaction(
                tx.transactionId(),
                txAsString,
                /* We voted yes. */
                1,
                destinationCount,
                true
            )
        );
    }

    /**
     * Precondition: the message has no local processing left to do.
     *
     * <p>
     * If {@code destinations} contains our bank ID, it will be ignored. If {@code destinations}
     * must not be only our bank.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    private void queueOutgoingMessage(Message message, Set<Long> destinations) {
        final String messageAsString;
        try {
            messageAsString = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new MessagePrepFailedException(e);
        }

        var didSend = false;
        for (final var dest : destinations) {
            if (dest == ForeignBankId.OUR_ROUTING_NUMBER) continue;
            didSend = true;
            outboxRepo.save(
                new OutboxMessage(
                    message.idempotenceKey(),
                    dest,
                    messageAsString,
                    false,
                    Instant.MIN
                )
            );
        }
        if (didSend)
            /* Probably forgot to do something. */
            throw new IllegalStateException("Didn't properly handle a lack of local message?");
    }

    @Override
    public ForeignBankId submitTx(final Transaction tx_) {
        final var destinations = collectAndValidateDestinations(tx_);
        final var tx = tx_.withTransactionId(ForeignBankId.our(UUID.randomUUID()));
        if (!destinations.contains(ForeignBankId.OUR_ROUTING_NUMBER))
            throw new IllegalArgumentException("Transaction is not in our bank");

        synchronized (transactionKey) {
            /* Needs to be synced due to he use of executeLocalPhase1. */
            txTemplate.executeWithoutResult(status -> {
                final var complaints = executeLocalPhase1(tx);
                if (!complaints.isEmpty()) {
                    /* Doom the transaction. */
                    status.setRollbackOnly();
                    throw new TxLocalPartVotedNo(tx, complaints);
                }

                /* We voted yes. */
                final var idempotenceKey = newIdempotenceKey();
                final var recordedTx = recordTx(tx, destinations.size());

                /* Are we the only ones? */
                if (destinations.equals(Set.of(ForeignBankId.OUR_ROUTING_NUMBER))) {
                    /* Yes. Finish the TX. */
                    assert recordedTx.isVotesAreYes()
                        && recordedTx.getVotesCast() == 1
                        && recordedTx.getNeededVotes() == 1
                           : "Expected new transaction to be complete (there's only one destination)";
                    executeLocalPhase2(tx);
                } else {
                    /* No. Tell the others. */
                    assert recordedTx.getVotesCast() == 1 && recordedTx.getNeededVotes() > 1;
                    queueOutgoingMessage(new Message.NewTx(idempotenceKey, tx), destinations);
                }
            });
        }

        processOutbox();

        return tx.transactionId();
    }

    @Override
    @Transactional(
        /* does this make sense even? */ isolation = Isolation.SERIALIZABLE
    )
    public ForeignBankId submitImmediateTx(final Transaction tx_) {
        final var tx = tx_.withTransactionId(ForeignBankId.our(UUID.randomUUID()));
        final var destinations = collectAndValidateDestinations(tx);
        if (!destinations.contains(ForeignBankId.OUR_ROUTING_NUMBER))
            throw new IllegalArgumentException("Transaction is not in our bank");
        if (destinations.size() == 1)
            throw new IllegalArgumentException("Transaction is not fully in our bank");

        synchronized (transactionKey) {
            final var complaints = executeLocalPhase1(tx);
            if (!complaints.isEmpty()) throw new TxLocalPartVotedNo(tx, complaints);

            recordTx(tx, 1);

            executeLocalPhase2(tx);

            return tx.transactionId();
        }
    }

    /* =============================== Inter-bank processing. =============================== */
    @Async("txExecutorPool")
    private void processOutbox() {
        final List<Pair<Long, String>> toResend;
        synchronized (transactionKey) {
            final var lastSendInstant =
                Instant.now()
                    .minus(interbankConfig.getResendDuration());
            toResend = txTemplate.execute(new TransactionCallback<List<Pair<Long, String>>>() {
                @Override
                public List<Pair<Long, String>> doInTransaction(TransactionStatus status) {
                    final var messages = outboxRepo.findAllSentBefore(lastSendInstant);
                    messages.forEach(m -> m.setLastSendTime(lastSendInstant));
                    outboxRepo.saveAll(messages);
                    return messages.stream()
                        .map(m -> Pair.of(m.getDestination(), m.getMessageBody()))
                        .toList();
                }
            });
        }

        /* TODO(arsen): deliver toResend. */
        log.debug("need to resend {}", toResend);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.debug("Starting IBEX with config {}", interbankConfig);
        taskScheduler.scheduleAtFixedRate(
            () -> processOutbox(),
            interbankConfig.getResendDuration()
                .dividedBy(2)
        );
    }
}
