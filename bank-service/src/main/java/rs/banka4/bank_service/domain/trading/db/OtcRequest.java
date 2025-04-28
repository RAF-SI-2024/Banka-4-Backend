package rs.banka4.bank_service.domain.trading.db;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;
import rs.banka4.bank_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.bank_service.domain.security.stock.db.Stock;

@Entity
@Table(name = "otc_requests")
@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class OtcRequest {
    @Id
    @Builder.Default
    private ForeignBankId id = ForeignBankId.our(UUID.randomUUID());

    @ManyToOne(optional = false)
    private Stock stock;

    @Column(nullable = false)
    private MonetaryAmount pricePerStock;

    @Column(nullable = false)
    private MonetaryAmount premium;

    @Column(nullable = false)
    private int amount;

    /** trust me bro key to users */
    @Column(nullable = false)
    private ForeignBankId madeBy;

    /** trust me bro key to users */
    @Column(nullable = false)
    private ForeignBankId madeFor;

    /** trust me bro key to users */
    @Column(nullable = false)
    private ForeignBankId modifiedBy;

    @Column(nullable = false)
    private LocalDate settlementDate;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime lastModified;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    private UUID optionId;
}
