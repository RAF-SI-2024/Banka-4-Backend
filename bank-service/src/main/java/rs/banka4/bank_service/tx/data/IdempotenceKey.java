package rs.banka4.bank_service.tx.data;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record IdempotenceKey(
    @Column(nullable = false) long routingNumber,
    @Column(nullable = false) String locallyGeneratedKey
) {
}
