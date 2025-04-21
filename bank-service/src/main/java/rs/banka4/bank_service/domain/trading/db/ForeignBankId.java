package rs.banka4.bank_service.domain.trading.db;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.UUID;

@Embeddable
/* @formatter:off */
public record ForeignBankId(
    /**
     * Bank identifier: example for our bank - 444
     */
    @Column(nullable = false) long routingNumber,
    @Column(nullable = false) String id
) {
/* @formatter:on */
    public static final long OUR_ROUTING_NUMBER = 444L;

    public static ForeignBankId our(UUID localPart) {
        return new ForeignBankId(OUR_ROUTING_NUMBER, localPart.toString());
    }

    /** @deprecated Use {@link #id()}. */
    @Deprecated
    public String userId() {
        return id();
    }
}
