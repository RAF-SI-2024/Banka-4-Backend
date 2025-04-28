package rs.banka4.bank_service.tx.executor.db;

import jakarta.persistence.Embeddable;
import rs.banka4.bank_service.tx.data.IdempotenceKey;

@Embeddable
public record OutboxMessageId(
    IdempotenceKey idempotenceKey,
    long destination
) {
}
