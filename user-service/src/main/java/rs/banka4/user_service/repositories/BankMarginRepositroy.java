package rs.banka4.user_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.banka4.user_service.domain.loan.db.BankMargin;
import rs.banka4.user_service.domain.loan.db.LoanType;

import java.util.Optional;

public interface BankMarginRepositroy extends JpaRepository<BankMargin, Long> {
    Optional<BankMargin> findBankMarginByType(LoanType type);
}
