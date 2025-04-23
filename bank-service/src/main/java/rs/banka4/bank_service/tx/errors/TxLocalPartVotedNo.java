package rs.banka4.bank_service.tx.errors;

import java.util.Collections;
import java.util.List;
import lombok.Value;
import rs.banka4.bank_service.tx.data.DoubleEntryTransaction;
import rs.banka4.bank_service.tx.data.NoVoteReason;

@Value
public class TxLocalPartVotedNo extends TxException {
    private DoubleEntryTransaction transaction;
    private List<NoVoteReason> reasons;

    public TxLocalPartVotedNo(DoubleEntryTransaction transaction, List<NoVoteReason> reasons) {
        super("Local part of a transaction %s voted no".formatted(transaction));
        this.transaction = transaction;
        this.reasons = Collections.unmodifiableList(reasons);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
