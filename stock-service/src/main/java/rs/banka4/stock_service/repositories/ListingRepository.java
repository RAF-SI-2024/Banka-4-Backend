package rs.banka4.stock_service.repositories;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rs.banka4.stock_service.domain.listing.db.Listing;
import rs.banka4.stock_service.domain.security.Security;

@Repository
public interface ListingRepository extends
    JpaRepository<Listing, UUID>,
    JpaSpecificationExecutor<Listing> {

    @Query(
        value = "select l from Listing l where l.security.id = :securityId order by l.lastRefresh desc"
    )
    Optional<Listing> getLatestListing(UUID securityId, Limit limit);

    Listing findListingBySecurity(Security security);

    Optional<Listing> findBySecurityIdAndActiveTrue(UUID assetId);

}
