package rs.banka4.bank_service.tx.executor.db;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.banka4.bank_service.tx.data.IdempotenceKey;

@Data
@Entity
@Table(name = "inbox")
@AllArgsConstructor
@NoArgsConstructor
public class InboxMessage {
    @EmbeddedId
    private IdempotenceKey key;

    @Column(
        /* If null, the response is empty. */
        nullable = true,
        columnDefinition = "jsonb"
    )
    /** Encoded response body. */
    private String responseBody;
}
