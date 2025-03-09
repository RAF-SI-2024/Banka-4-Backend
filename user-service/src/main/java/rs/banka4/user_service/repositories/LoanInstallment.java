package rs.banka4.user_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LoanInstallment extends JpaRepository<rs.banka4.user_service.domain.loan.db.LoanInstallment, UUID> {
}
