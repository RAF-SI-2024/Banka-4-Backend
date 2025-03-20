package rs.banka4.stock_service.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.banka4.stock_service.domain.listing.db.ListingDailyPriceInfo;

@Repository
public interface ListingDailyPriceInfoRepository extends
    JpaRepository<ListingDailyPriceInfo, UUID> {
}
