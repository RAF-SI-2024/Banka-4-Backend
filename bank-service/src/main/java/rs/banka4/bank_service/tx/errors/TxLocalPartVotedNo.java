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

    public TxLocalPartVotedNo(DoubleEntryTransaction tx, List<NoVoteReason> reasons) {
        super("Local part of transaction %s voted no. Reasons: %s".formatted(tx, reasons));
        this.transaction = tx;
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
