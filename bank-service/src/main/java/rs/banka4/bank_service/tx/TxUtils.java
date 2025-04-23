package rs.banka4.bank_service.tx;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;
import rs.banka4.bank_service.tx.data.Posting;
import rs.banka4.bank_service.tx.data.Transaction;
import rs.banka4.bank_service.tx.data.TxAccount;
import rs.banka4.bank_service.tx.data.TxAsset;

/** Various utilities for working with transactions. */
public class TxUtils {
    public static boolean isTxBalanced(Transaction tx) {
        final var txBalance = new HashMap<TxAsset, BigDecimal>();
        tx.postings()
            .forEach(p -> txBalance.merge(p.asset(), p.amount(), BigDecimal::add));
        return txBalance.values()
            .stream()
            .allMatch(BigDecimal.ZERO::equals);
    }

    public static Set<Long> collectDestinations(final Transaction tx) {
        return tx.postings()
            .stream()
            .map(Posting::account)
            .map(TxAccount::routingNumber)
            .collect(Collectors.toSet());
    }
}
