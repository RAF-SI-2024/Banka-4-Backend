package rs.banka4.bank_service.tx.executor.db;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;

@Data
@Entity
@Table(name = "active_tx")
@AllArgsConstructor
@NoArgsConstructor
public class ExecutingTransaction {
    @EmbeddedId
    private ForeignBankId id;

    /* lol */
    @Column(
        nullable = false,
        columnDefinition = "jsonb"
    )
    private String txObject;

    private int votesCast;
    private int neededVotes;
    private boolean votesAreYes;
}
