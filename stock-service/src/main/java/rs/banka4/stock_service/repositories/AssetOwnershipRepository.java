package rs.banka4.stock_service.repositories;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rs.banka4.stock_service.domain.assets.db.AssetOwnership;
import rs.banka4.stock_service.domain.assets.db.AssetOwnershipId;

public interface AssetOwnershipRepository extends JpaRepository<AssetOwnership, AssetOwnershipId> {
    @Query(
        value = "select a from AssetOwnership a where a.id.user = :userId and a.id.asset.id = :assetId"
    )
    Optional<AssetOwnership> findByMyId(UUID userId, UUID assetId);
}
