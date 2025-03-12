package rs.banka4.user_service.repositories;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.banka4.user_service.domain.loan.db.BankMargin;
import rs.banka4.user_service.domain.loan.db.LoanType;

@Repository
public interface BankMarginRepository extends JpaRepository<BankMargin, UUID> {
    Optional<BankMargin> findBankMarginByType(LoanType type);
}
