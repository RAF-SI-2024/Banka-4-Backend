package rs.banka4.bank_service.domain.assets.db;

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

    public String getTicker() {
        return getId().getAsset()
            .getTicker();
    }
}
