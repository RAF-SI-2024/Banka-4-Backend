package rs.banka4.bank_service.repositories;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import rs.banka4.bank_service.domain.listing.db.ListingDailyPriceInfo;

@Repository
public interface ListingDailyPriceInfoRepository extends
    JpaRepository<ListingDailyPriceInfo, UUID> {
    @Query(
        value = "select l from ListingDailyPriceInfo l where l.security.id = :securityId order by l.date desc"
    )
    Optional<ListingDailyPriceInfo> getYesterdayListingDailyPriceInfo(UUID securityId, Limit limit);

    @Query("""
            select l from ListingDailyPriceInfo l
            where l.security.id = :securityId
            and l.date >= :startOfDay
            and l.date < :startOfNextDay
        """)
    Optional<List<ListingDailyPriceInfo>> getListingDailyPriceInfoForDate(
        @Param("securityId") UUID securityId,
        @Param("startOfDay") OffsetDateTime startOfDay,
        @Param("startOfNextDay") OffsetDateTime startOfNextDay
    );

    @Modifying
    @Transactional
    @Query("""
        delete from ListingDailyPriceInfo l
        where l.security.id = :securityId
        and l.date >= :startOfDay
        and l.date < :startOfNextDay
        """)
    void deleteListingDailyPriceInfoForDate(
        @Param("securityId") UUID securityId,
        @Param("startOfDay") OffsetDateTime startOfDay,
        @Param("startOfNextDay") OffsetDateTime startOfNextDay
    );


    List<ListingDailyPriceInfo> findAllBySecurityId(UUID securityId);
}
