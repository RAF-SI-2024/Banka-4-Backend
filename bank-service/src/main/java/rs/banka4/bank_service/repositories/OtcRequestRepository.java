package rs.banka4.bank_service.repositories;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;
import rs.banka4.bank_service.domain.trading.db.OtcRequest;
import rs.banka4.bank_service.domain.trading.db.RequestStatus;

public interface OtcRequestRepository extends JpaRepository<OtcRequest, ForeignBankId> {

    @Query(
        "SELECT o FROM OtcRequest o "
            + "WHERE o.status = 'ACTIVE' "
            + "AND (o.madeFor.id = :userId OR o.madeBy.id = :userId)"
            + "ORDER BY o.lastModified DESC"
    )
    Page<OtcRequest> findActiveRequestsByUser(@Param("userId") String userId, Pageable pageable);

    @Query(
        "SELECT o FROM OtcRequest o "
            + "WHERE o.status = 'ACTIVE' "
            + "AND (o.madeFor.id = :userId OR o.madeBy.id = :userId) "
            + "AND o.modifiedBy.id <> :userId "
            + "ORDER BY o.lastModified DESC"
    )
    Page<OtcRequest> findActiveUnreadRequestsByUser(
        @Param("userId") String userId,
        Pageable pageable
    );

    Optional<OtcRequest> findByOptionId(UUID optionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM OtcRequest o WHERE o.optionId = :optionId")
    Optional<OtcRequest> findAndLockByOptionId(UUID optionId);

    List<OtcRequest> findAllByStatusAndSettlementDateBefore(RequestStatus status, LocalDate date);
}
