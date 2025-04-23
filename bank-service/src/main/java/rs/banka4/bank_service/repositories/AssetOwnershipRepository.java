package rs.banka4.bank_service.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.banka4.bank_service.domain.assets.db.AssetOwnership;
import rs.banka4.bank_service.domain.assets.db.AssetOwnershipId;

public interface AssetOwnershipRepository extends JpaRepository<AssetOwnership, AssetOwnershipId> {
    @Query(
        value = "select a from AssetOwnership a where a.id.user.id = :userId and a.id.asset.id = :assetId"
    )
    Optional<AssetOwnership> findByMyId(UUID userId, UUID assetId);

    Page<AssetOwnership> findAllByPublicAmountGreaterThan(int publicAmount, Limit limit);

    @Query(
        "SELECT ao FROM AssetOwnership ao WHERE ao.id.user.id = :userId AND (ao.privateAmount > 0 OR ao.publicAmount > 0)"
    )
    Page<AssetOwnership> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query(
        "SELECT ao FROM AssetOwnership ao WHERE ao.id.user.id = :userId AND (ao.privateAmount > 0 OR ao.publicAmount > 0)"
    )
    List<AssetOwnership> findByUserId(@Param("userId") UUID userId);
}
