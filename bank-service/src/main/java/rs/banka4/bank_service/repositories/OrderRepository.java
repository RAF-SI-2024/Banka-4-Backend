package rs.banka4.bank_service.repositories;

import jakarta.persistence.LockModeType;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.banka4.bank_service.domain.options.db.Asset;
import rs.banka4.bank_service.domain.orders.db.Direction;
import rs.banka4.bank_service.domain.orders.db.Order;
import rs.banka4.bank_service.domain.orders.db.Status;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    @Query(
        value = "select count(*) from Order o where o.asset.id = ?1 and o.createdAt between ?2 and ?3"
    )
    int findCountOfMadeOrdersToday(UUID assetId, OffsetDateTime startDate, OffsetDateTime endDate);

    List<Order> findAllByStatusAndIsDoneFalse(Status status);

    @Query(
        "SELECT o FROM Order o "
            + "WHERE o.asset.id = :assetId "
            + "AND o.direction = :direction "
            + "AND o.status = :status "
            + "AND o.isDone = false "
            + "AND o.remainingPortions >= :quantity "
            + "ORDER BY CASE WHEN :direction = 'BUY' THEN o.pricePerUnit.amount ELSE -o.pricePerUnit.amount END ASC"
    )
    List<Order> findMatchingOrders(
        @Param("assetId") UUID assetId,
        @Param("direction") Direction direction,
        @Param("status") Status status,
        @Param("quantity") int quantity,
        Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :orderId")
    Optional<Order> findByIdWithLock(@Param("orderId") UUID orderId);

    List<Order> findByUserId(UUID userId);

    List<Order> findByUserIdAndAssetAndDirectionAndIsDone(
        UUID userId,
        Asset asset,
        Direction direction,
        boolean isDone
    );

    List<Order> findByUserIdAndDirectionAndIsDone(UUID userId, Direction direction, boolean isDone);

    @Query("""
             SELECT o FROM Order o
             WHERE o.user.id = :userId
             AND o.asset = :asset
             AND o.isDone = :isDone
             ORDER BY o.lastModified DESC
        """)
    Optional<Order> findNewestOrder(
        @Param("userId") UUID userId,
        @Param("asset") Asset asset,
        @Param("isDone") boolean isDone
    );

    Page<Order> findAllByStatusIn(List<Status> statuses, Pageable pageable);
}
