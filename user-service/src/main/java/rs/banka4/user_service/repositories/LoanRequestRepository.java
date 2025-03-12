package rs.banka4.user_service.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.banka4.user_service.domain.loan.db.LoanRequest;

@Repository
public interface LoanRequestRepository extends JpaRepository<LoanRequest, UUID> {


}
