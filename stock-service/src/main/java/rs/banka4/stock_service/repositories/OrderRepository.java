package rs.banka4.stock_service.repositories;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.banka4.stock_service.domain.options.db.Asset;
import rs.banka4.stock_service.domain.orders.db.Direction;
import rs.banka4.stock_service.domain.orders.db.Order;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    @Query(
        value = "select count(*) from Order o where o.asset.id = ?1 and o.createdAt between ?2 and ?3"
    )
    int findCountOfMadeOrdersToday(UUID assetId, OffsetDateTime startDate, OffsetDateTime endDate);

    List<Order> findByUserId(UUID userId);

    List<Order> findByUserIdAndAssetAndDirectionAndIsDone(
        UUID userId,
        Asset asset,
        Direction direction,
        boolean isDone
    );

    @Query("""
             SELECT o FROM Order o
             WHERE o.userId = :userId
             AND o.asset = :asset
             AND o.direction = :direction
             AND o.isDone = :isDone
             ORDER BY o.lastModified DESC
        """)
    Order findNewestOrder(
        @Param("userId") UUID userId,
        @Param("asset") Asset asset,
        @Param("direction") Direction direction,
        @Param("isDone") boolean isDone
    );

}
