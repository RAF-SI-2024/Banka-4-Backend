package rs.banka4.stock_service.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rs.banka4.stock_service.domain.listing.db.ListingDailyPriceInfo;

@Repository
public interface ListingDailyPriceInfoRepository extends
    JpaRepository<ListingDailyPriceInfo, UUID> {
    @Query(
        value = "select l from ListingDailyPriceInfo l where l.security.id = :securityId order by l.date desc"
    )
    Optional<ListingDailyPriceInfo> getYesterdayListingDailyPriceInfo(UUID securityId, Limit limit);

    List<ListingDailyPriceInfo> findAllBySecurityId(UUID securityId);
}
