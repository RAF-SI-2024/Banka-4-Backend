package rs.banka4.stock_service.domain.assets.db;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "asset_ownership")
public class AssetOwnership {
    @EmbeddedId
    private AssetOwnershipId id;

    @Column(nullable = false)
    private int privateAmount;

    @Column(nullable = false)
    private int publicAmount;

    @Column(nullable = false)
    private int reservedAmount;
}
