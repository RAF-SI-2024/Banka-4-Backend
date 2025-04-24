package rs.banka4.bank_service.tx.executor;

import static java.util.function.Predicate.*;
import static rs.banka4.bank_service.tx.TxUtils.isTxBalanced;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import lombok.SneakyThrows;
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
import rs.banka4.bank_service.domain.assets.db.AssetOwnershipId;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;
import rs.banka4.bank_service.domain.user.User;
import rs.banka4.bank_service.repositories.AccountRepository;
import rs.banka4.bank_service.repositories.AssetOwnershipRepository;
import rs.banka4.bank_service.repositories.StockRepository;
import rs.banka4.bank_service.repositories.UserRepository;
import rs.banka4.bank_service.tx.TxExecutor;
import rs.banka4.bank_service.tx.TxUtils;
import rs.banka4.bank_service.tx.config.InterbankConfig;
import rs.banka4.bank_service.tx.data.CommitTransaction;
import rs.banka4.bank_service.tx.data.DoubleEntryTransaction;
import rs.banka4.bank_service.tx.data.IdempotenceKey;
import rs.banka4.bank_service.tx.data.Message;
import rs.banka4.bank_service.tx.data.MonetaryAsset;
import rs.banka4.bank_service.tx.data.NoVoteReason;
import rs.banka4.bank_service.tx.data.OptionDescription;
import rs.banka4.bank_service.tx.data.Posting;
import rs.banka4.bank_service.tx.data.RollbackTransaction;
import rs.banka4.bank_service.tx.data.StockDescription;
import rs.banka4.bank_service.tx.data.TransactionVote;
import rs.banka4.bank_service.tx.data.TxAccount;
import rs.banka4.bank_service.tx.data.TxAsset;
import rs.banka4.bank_service.tx.errors.MessagePrepFailedException;
import rs.banka4.bank_service.tx.errors.TxLocalPartVotedNo;
import rs.banka4.bank_service.tx.executor.db.ExecutingTransaction;
import rs.banka4.bank_service.tx.executor.db.ExecutingTransactionRepository;
import rs.banka4.bank_service.tx.executor.db.InboxMessage;
import rs.banka4.bank_service.tx.executor.db.InboxRepository;
import rs.banka4.bank_service.tx.executor.db.OutboxMessage;
import rs.banka4.bank_service.tx.executor.db.OutboxMessageId;
import rs.banka4.bank_service.tx.executor.db.OutboxRepository;
import rs.banka4.bank_service.tx.otc.config.InterbankRetrofitProvider;

/**
 * A transaction executor capable of delivering transactions across banks.
 */
@Service
@Slf4j
public class InterbankTxExecutor implements TxExecutor, ApplicationRunner {
    private final InterbankConfig interbankConfig;
    private final TransactionTemplate txTemplate;
    private final TransactionTemplate txNestedTemplate;
    private final ObjectMapper objectMapper;
    private final AccountRepository accountRepo;
    private final OutboxRepository outboxRepo;
    private final ExecutingTransactionRepository execTxRepo;
    private final TaskScheduler taskScheduler;
    private final EntityManager entityManager;
    private final InterbankRetrofitProvider interbanks;
    private final InboxRepository inboxRepo;
    private final UserRepository userRepo;
    private final StockRepository stockRepo;
    private final AssetOwnershipRepository assetOwnershipRepo;

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
        EntityManager entityManager,
        InterbankRetrofitProvider interbanks,
        InboxRepository inboxRepo,
        UserRepository userRepo,
        StockRepository stockRepo,
        AssetOwnershipRepository assetOwnershipRepo
    ) {
        this.interbankConfig = config;
        this.txTemplate = new TransactionTemplate(transactionManager);
        this.txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.txTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);

        this.txNestedTemplate = new TransactionTemplate(transactionManager);
        this.txNestedTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_NESTED);
        this.txNestedTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);

        this.objectMapper = objectMapper;
        this.accountRepo = accountRepo;
        this.outboxRepo = outboxRepo;
        this.execTxRepo = execTxRepo;
        this.taskScheduler = taskScheduler;
        this.entityManager = entityManager;
        this.interbanks = interbanks;
        this.inboxRepo = inboxRepo;
        this.userRepo = userRepo;
        this.stockRepo = stockRepo;
        this.assetOwnershipRepo = assetOwnershipRepo;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    protected IdempotenceKey newIdempotenceKey() {
        final var key =
            new IdempotenceKey(
                ForeignBankId.OUR_ROUTING_NUMBER,
                UUID.randomUUID()
                    .toString()
            );

        /* TODO(arsen): detect reuse */
        return key;
    }

    private Set<Long> collectAndValidateDestinations(DoubleEntryTransaction tx) {
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

    private Optional<NoVoteReason> personStockPostingPhase1(
        User person,
        StockDescription assetDescription,
        Posting posting
    ) {
        final var asset_ = stockRepo.findByTicker(assetDescription.ticker());
        if (asset_.isEmpty()) return Optional.of(new NoVoteReason.NoSuchAsset(posting));
        final var asset = asset_.get();

        final int amount;
        try {
            amount =
                posting.amount()
                    .intValueExact();
        } catch (ArithmeticException ignored) {
            return Optional.of(new NoVoteReason.InsufficientAsset(posting));
        }

        if (amount >= 0)
            /* Nothing to reserve, this is a debit. */
            return Optional.empty();

        final var assetOwnership_ =
            assetOwnershipRepo.findAndLockById(new AssetOwnershipId(person, asset));
        if (assetOwnership_.isEmpty())
            /* The user never had any. */
            return Optional.of(new NoVoteReason.InsufficientAsset(posting));
        final var assetOwnership = assetOwnership_.get();

        /* Transfers must come from privately-posessed stocks. Amount is negative. */
        final var newPrivateStockAmount = assetOwnership.getPrivateAmount() + amount;
        if (newPrivateStockAmount < 0)
            return Optional.of(new NoVoteReason.InsufficientAsset(posting));

        /* Commit the reservation. */
        assetOwnership.setPrivateAmount(newPrivateStockAmount);
        assetOwnership.setReservedAmount(assetOwnership.getReservedAmount() - amount);
        assetOwnershipRepo.save(assetOwnership);
        return Optional.empty();
    }

    private void personStockPostingPhase1Rollback(
        User person,
        StockDescription assetDescription,
        Posting posting
    ) {
        final var asset =
            stockRepo.findByTicker(assetDescription.ticker())
                .orElseThrow(() -> new IllegalStateException("invalid tx?"));

        final var assetOwnership =
            assetOwnershipRepo.findAndLockById(new AssetOwnershipId(person, asset))
                .orElseThrow(() -> new IllegalStateException("invalid tx?"));

        final int amount;
        try {
            amount =
                posting.amount()
                    .intValueExact();
        } catch (ArithmeticException e) {
            throw new IllegalStateException("invalid tx?", e);
        }

        if (amount >= 0) return;

        /* Roll back the reservation. */
        assetOwnership.setPrivateAmount(assetOwnership.getPrivateAmount() - amount);
        assetOwnership.setReservedAmount(assetOwnership.getReservedAmount() + amount);
        assetOwnershipRepo.save(assetOwnership);
    }

    private void personStockPostingPhase2(
        User person,
        StockDescription assetDescription,
        Posting posting
    ) {
        final var asset =
            stockRepo.findByTicker(assetDescription.ticker())
                .orElseThrow(() -> new IllegalStateException("invalid tx?"));

        final var assetOwnership =
            assetOwnershipRepo.findAndLockById(new AssetOwnershipId(person, asset))
                .orElseThrow(() -> new IllegalStateException("invalid tx?"));

        final int amount;
        try {
            amount =
                posting.amount()
                    .intValueExact();
        } catch (ArithmeticException e) {
            throw new IllegalStateException("invalid tx?", e);
        }

        /* @formatter:off
         * By reasoning similar to Note [Phase-by-phase balance changes], let P be the private
         * reservation amount before phase 1, P' be private ownership amount after phase 1, and
         * P_r be private ownership amount after phase 2.  Let, also, R with respective
         * indices/superscripts indicate the same states of ownership reservation.  Let X be the
         * amount of the posting.
         *
         * Our goal is that P_r = P + X, and R_r = R.
         *
         * P' = P + \min(X, 0), and R' = P - \min(X, 0) as a result of P' = P and R' = R if X >= 0,
         * and P' = P + X and R' = R - X otherwise
         *
         * Lets express P and R in terms of P' and R'.  P = P' - \min(X, 0); R = R' + \min(X, 0).
         * Hence, P_r = P' - \min(X, 0) + X; R_r = R' + \min(X, 0).  By reasoning similar to Note
         * [Phase-by-phase balance changes].
         *
         * @formatter:on
         */

        assetOwnership.setReservedAmount(assetOwnership.getReservedAmount() + Math.min(amount, 0));
        assetOwnership.setPrivateAmount(
            assetOwnership.getPrivateAmount() - Math.min(amount, 0) + amount
        );
        assetOwnershipRepo.save(assetOwnership);
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
     * Executes in a nested transaction, so local changes should be reverted.
     *
     * <p>
     * Caller should hold {@link #transactionKey}.
     *
     * @throws TxLocalPartVotedNo if the local part of this transaction fails
     */
    private void executeLocalPhase1(DoubleEntryTransaction tx) {
        if (!isTxBalanced(tx))
            throw new TxLocalPartVotedNo(tx, List.of(new NoVoteReason.UnbalancedTx()));

        txNestedTemplate.executeWithoutResult(s -> {
            final var noReasons = new ArrayList<NoVoteReason>();
            for (final var posting : tx.postings()) {
                if (
                    posting.account()
                        .routingNumber()
                        != ForeignBankId.OUR_ROUTING_NUMBER
                ) continue;

                switch (posting.account()) {
                case TxAccount.Person(ForeignBankId personId) -> {
                    final var person_ = userRepo.findById(UUID.fromString(personId.id()));
                    if (!person_.isPresent()) {
                        noReasons.add(new NoVoteReason.NoSuchAccount(posting));
                        continue;
                    }
                    final var person = person_.get();

                    switch (posting.asset()) {
                    case TxAsset.Monas(MonetaryAsset asset)
                        -> throw new IllegalArgumentException(
                            "must preprocess tx with resolvePersonMonetaryAssetPostings"
                        );
                    case TxAsset.Stock(StockDescription asset)
                        -> personStockPostingPhase1(person, asset, posting).ifPresent(
                            noReasons::add
                        );
                    case TxAsset.Option(OptionDescription asset)
                        -> throw new NotImplementedException();
                    }
                }
                case TxAccount.Option(ForeignBankId optionId) -> {
                    throw new NotImplementedException();
                }

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

                case TxAccount.MemoryHole() -> {
                    /* Always OK. */
                }
                }
            }

            if (!noReasons.isEmpty()) {
                s.setRollbackOnly();
                throw new TxLocalPartVotedNo(tx, noReasons);
            }
        });
    }

    /**
     * Roll back phase one of local transaction execution:
     *
     * <blockquote>
     * <p>
     * Should a transaction fail after it has been
     * <a href="https://arsen.srht.site/si-tx-proto/#orga35c5c5">locally prepared</a>, it can be
     * rolled back by un-reserving all resources reserved by local credit postings in the original
     * transaction. The transaction should also be marked as failed in the transaction log.
     * </p>
     *
     * <p>
     * It is impossible for a transaction to be rolled back after a
     * <a href="https://arsen.srht.site/si-tx-proto/#org09040a6">local commit</a>.
     * </p>
     * </blockquote>
     *
     * <p>
     * Caller should hold {@link #transactionKey}.
     *
     * @returns A list of reasons not to accept a transaction. The caller is expected to make the
     *          transaction roll back if the list is non-empty.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    protected void rollbackLocalPhase1(DoubleEntryTransaction tx) {
        for (final var posting : tx.postings()) {
            if (
                posting.account()
                    .routingNumber()
                    != ForeignBankId.OUR_ROUTING_NUMBER
            ) continue;

            switch (posting.account()) {
            case TxAccount.Person(ForeignBankId personId) -> {
                final var person =
                    userRepo.findById(UUID.fromString(personId.id()))
                        .orElseThrow(() -> new IllegalStateException("invalid tx"));
                switch (posting.asset()) {
                case TxAsset.Monas(MonetaryAsset asset)
                    -> throw new IllegalArgumentException(
                        "must preprocess tx with resolvePersonMonetaryAssetPostings before P1"
                    );
                case TxAsset.Stock(StockDescription asset)
                    -> personStockPostingPhase1Rollback(person, asset, posting);
                case TxAsset.Option(OptionDescription asset) -> throw new NotImplementedException();
                }
            }
            case TxAccount.Option(ForeignBankId optionId) -> throw new NotImplementedException();

            case TxAccount.Account(String accNumber) -> {
                final var acc =
                    accountRepo.findAccountByAccountNumber(accNumber)
                        .orElseThrow(() -> new IllegalStateException("Invalid tx?"));
                entityManager.lock(acc, LockModeType.PESSIMISTIC_WRITE);
                entityManager.refresh(acc);

                acc.setAvailableBalance(
                    acc.getAvailableBalance()
                        .subtract(
                            posting.amount()
                                .min(BigDecimal.ZERO)
                        )
                );
                accountRepo.save(acc);
            }

            case TxAccount.MemoryHole() -> {
                /* Always OK. */
            }
            }
        }
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
    protected void executeLocalPhase2(DoubleEntryTransaction tx) {
        for (final var posting : tx.postings()) {
            if (
                posting.account()
                    .routingNumber()
                    != ForeignBankId.OUR_ROUTING_NUMBER
            ) continue;

            switch (posting.account()) {
            case TxAccount.Person(ForeignBankId personId) -> {
                final var person =
                    userRepo.findById(UUID.fromString(personId.id()))
                        .orElseThrow(() -> new IllegalStateException("invalid tx"));
                switch (posting.asset()) {
                case TxAsset.Monas(MonetaryAsset asset)
                    -> throw new IllegalArgumentException(
                        "must preprocess tx with resolvePersonMonetaryAssetPostings before P1"
                    );
                case TxAsset.Stock(StockDescription asset)
                    -> personStockPostingPhase2(person, asset, posting);
                case TxAsset.Option(OptionDescription asset) -> throw new NotImplementedException();
                }
            }
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

            case TxAccount.MemoryHole() -> {
                /* Always OK. */
            }
            }
        }
    }

    /**
     * Records the transaction with a single vote, presuming that we voted yes locally.
     *
     * @return The local transaction
     */
    @Transactional(propagation = Propagation.MANDATORY)
    protected ExecutingTransaction recordTx(DoubleEntryTransaction tx, int neededVotes) {
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
                neededVotes,
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
    protected void queueOutgoingMessage(Message message, Set<Long> destinations) {
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
                    new OutboxMessageId(message.idempotenceKey(), dest),
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
    public ForeignBankId submitTx(final DoubleEntryTransaction tx_) {
        final var destinations = collectAndValidateDestinations(tx_);
        final var tx = tx_.withTransactionId(ForeignBankId.our(UUID.randomUUID()));
        if (!destinations.contains(ForeignBankId.OUR_ROUTING_NUMBER))
            throw new IllegalArgumentException("Transaction is not in our bank");

        synchronized (transactionKey) {
            /* Needs to be synced due to he use of executeLocalPhase1. */
            txTemplate.executeWithoutResult(status -> {
                executeLocalPhase1(tx);

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
    public ForeignBankId submitImmediateTx(final DoubleEntryTransaction tx_) {
        final var tx = tx_.withTransactionId(ForeignBankId.our(UUID.randomUUID()));
        final var destinations = collectAndValidateDestinations(tx);
        if (!destinations.contains(ForeignBankId.OUR_ROUTING_NUMBER))
            throw new IllegalArgumentException("Transaction is not in our bank");
        if (destinations.size() == 1)
            throw new IllegalArgumentException("Transaction is not fully in our bank");

        synchronized (transactionKey) {
            executeLocalPhase1(tx);

            recordTx(tx, 1);

            executeLocalPhase2(tx);

            return tx.transactionId();
        }
    }

    /* =============================== Inter-bank processing. =============================== */

    /* Synchronization key for the outbox. We don't want multiple outboxings at once. */
    private final Object messageSendKey = new Object();

    @Async("txExecutorPool")
    private void processOutbox() {
        synchronized (messageSendKey) {
            final List<Pair<OutboxMessageId, String>> toResend;
            synchronized (transactionKey) {
                final var lastSendInstant =
                    Instant.now()
                        .minus(interbankConfig.getResendDuration());
                toResend =
                    txTemplate.execute(
                        new TransactionCallback<List<Pair<OutboxMessageId, String>>>() {
                            @Override
                            public List<Pair<OutboxMessageId, String>> doInTransaction(
                                TransactionStatus status
                            ) {
                                final var messages = outboxRepo.findAllSentBefore(lastSendInstant);
                                messages.forEach(m -> m.setLastSendTime(lastSendInstant));
                                outboxRepo.saveAll(messages);
                                return messages.stream()
                                    .map(m -> Pair.of(m.getMessageKey(), m.getMessageBody()))
                                    .toList();
                            }
                        }
                    );
            }

            log.debug("need to resend {}", toResend);
            for (final var msg : toResend) {
                try {
                    final var message = objectMapper.readValue(msg.getRight(), Message.class);
                    sendStoredMessage(msg.getLeft(), message);
                } catch (IOException e) {
                    log.error("cannot deliver message {}: {}", msg, e);
                }
            }
        }
    }

    private void sendStoredMessage(OutboxMessageId msgId, Message particularMessage)
        throws IOException {
        final var remote = interbanks.get(msgId.destination());
        final var response = (switch (particularMessage) {
        case Message.NewTx newTx -> remote.sendNewTx(newTx);
        case Message.CommitTx commitTx -> remote.sendCommit(commitTx);
        case Message.RollbackTx rollbackTx -> remote.sendRollback(rollbackTx);
        }).execute();

        if (response.code() == 202) {
            /* No response yet. The DB was already updated with the next resend time. */
            return;
        }

        if (!response.isSuccessful()) {
            /* Didn't manage to deliver. */
            log.error("Failed to deliver message {}: {}", msgId, response);
            return;
        }

        txTemplate.executeWithoutResult(status -> {
            if (response.body() instanceof TransactionVote txVote) {
                processVote(((Message.NewTx) particularMessage).message(), txVote);
            }
            outboxRepo.markAsDelivered(msgId);
        });
    }

    @Transactional(propagation = Propagation.MANDATORY)
    protected void processVote(DoubleEntryTransaction tx, TransactionVote txVote) {
        final var ongoingTx =
            execTxRepo.findAndLockTx(tx.transactionId())
                .orElseThrow(() -> new IllegalStateException("tx we sent vanished?"));

        ongoingTx.setVotesCast(ongoingTx.getVotesCast() + 1);

        if (txVote instanceof TransactionVote.No noVote) {
            /* This party voted no. */
            ongoingTx.setVotesAreYes(false);
            /* TODO(arsen): process reason */
            log.error("tx {} failed to execute due to {}", tx, noVote);
        }

        /*
         * If we just received the last vote, we should queue out the commits and rollbacks, as well
         * as perform our part of the commit.
         */

        if (ongoingTx.getNeededVotes() == ongoingTx.getVotesCast()) {
            if (ongoingTx.isVotesAreYes()) {
                /* Success. */
                executeLocalPhase2(tx);
                final var idempotenceKey = newIdempotenceKey();
                queueOutgoingMessage(
                    new Message.CommitTx(idempotenceKey, new CommitTransaction(tx.transactionId())),
                    collectAndValidateDestinations(tx)
                );
            } else {
                /* Someone said no. Roll back everyone. */
                final var idempotenceKey = newIdempotenceKey();
                rollbackLocalPhase1(tx);
                queueOutgoingMessage(
                    new Message.RollbackTx(
                        idempotenceKey,
                        new RollbackTransaction(tx.transactionId())
                    ),
                    collectAndValidateDestinations(tx)
                );
            }
        }
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

    /* Message reception. */
    protected <T, M extends Message> T doIdempotentMessageHandling(
        M msg,
        Class<T> responseType,
        Function<M, T> handler
    ) {
        final var idemKey = msg.idempotenceKey();
        final var isVoid = responseType.equals(Void.TYPE);
        synchronized (transactionKey) {
            return txTemplate.execute(new TransactionCallback<T>() {
                @Override
                @SneakyThrows
                public T doInTransaction(TransactionStatus status) {
                    final var prevMsg = inboxRepo.findAndLock(idemKey);
                    if (prevMsg.isPresent()) {
                        /* Previously-handled message. */
                        final var oldResp =
                            prevMsg.get()
                                .getResponseBody();
                        assert isVoid == (oldResp == null)
                               : "Body must be null iff response type is void";

                        if (isVoid)
                            /* It's void - simple case. */
                            return null;
                        return objectMapper.readValue(oldResp, responseType);
                    } else {
                        final var resp = handler.apply(msg);
                        inboxRepo.save(
                            new InboxMessage(
                                idemKey,
                                isVoid ? null : objectMapper.writeValueAsString(resp)
                            )
                        );
                        return resp;
                    }
                }
            });
        }
    }

    public TransactionVote processNewTxMessage(Message.NewTx message) {
        return doIdempotentMessageHandling(message, TransactionVote.class, m -> {
            final var tx = message.message();
            final var ectx = recordTx(tx, 2);
            try {
                executeLocalPhase1(tx);
                return new TransactionVote.Yes();
            } catch (TxLocalPartVotedNo reason) {
                ectx.setVotesAreYes(false);
                execTxRepo.save(ectx);
                return new TransactionVote.No(reason.getReasons());
            }
        });
    }

    public void processCommitOrRollbackMessage(Message message) {
        final var isCommit = switch (message) {
        case Message.CommitTx ignored -> true;
        case Message.RollbackTx ignored -> false;
        default -> throw new IllegalArgumentException();
        };
        final var txId = switch (message) {
        /* @formatter:off */
        case Message.CommitTx m -> m.message().transactionId();
        case Message.RollbackTx m -> m.message().transactionId();
        default -> throw new AssertionError("unreachable, but javac is being a silly billy :(");
        /* @formatter:on */
        };
        doIdempotentMessageHandling(message, TransactionVote.class, m -> {
            final var tx =
                execTxRepo.findById(txId)
                    .orElseThrow(() -> new IllegalStateException("Invalid tx?"));
            try {
                final var fullTx =
                    objectMapper.readValue(tx.getTxObject(), DoubleEntryTransaction.class);

                if (isCommit) executeLocalPhase2(fullTx);
                else if (tx.isVotesAreYes()) rollbackLocalPhase1(fullTx);

                assert tx.isVotesAreYes() || !isCommit
                       : "Votes-are-no -> Votes-are-yes edge invalid";

                tx.setVotesCast(tx.getNeededVotes());
                tx.setVotesAreYes(isCommit);
                execTxRepo.save(tx);
                return null;
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("invalid TX was persisted?", e);
            }
        });
    }
}
