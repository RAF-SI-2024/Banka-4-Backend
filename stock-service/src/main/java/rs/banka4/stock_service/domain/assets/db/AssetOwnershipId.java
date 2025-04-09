package rs.banka4.stock_service.domain.assets.db;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.banka4.stock_service.domain.options.db.Asset;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssetOwnershipId {
    @Column(nullable = false)
    private UUID user;

    @ManyToOne(optional = false)
    private Asset asset;
}
