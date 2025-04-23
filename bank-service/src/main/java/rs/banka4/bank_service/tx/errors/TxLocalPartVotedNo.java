package rs.banka4.bank_service.tx.errors;

import java.util.Collections;
import java.util.List;
import lombok.Value;
import rs.banka4.bank_service.tx.data.NoVoteReason;
import rs.banka4.bank_service.tx.data.Transaction;

@Value
public class TxLocalPartVotedNo extends TxException {
    private Transaction transaction;
    private List<NoVoteReason> reasons;

    public TxLocalPartVotedNo(Transaction transaction, List<NoVoteReason> reasons) {
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
