package rs.banka4.bank_service.tx.executor.db;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "outbox")
@AllArgsConstructor
@NoArgsConstructor
public class OutboxMessage {
    @EmbeddedId
    private OutboxMessageId messageKey;

    @Column(
        nullable = false,
        columnDefinition = "jsonb"
    )
    /** Encoded message body. Contains the idempotence key also, sadly. */
    private String messageBody;

    /**
     * Whether this message was sent and delivered. {@code false} indicates that this message should
     * be resent later.
     */
    private boolean delivered = false;

    /**
     * When did we last try to deliver this message?
     */
    private Instant lastSendTime;
}
