package rs.banka4.user_service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rs.banka4.user_service.domain.loan.db.Loan;
import rs.banka4.user_service.domain.loan.db.LoanRequest;

import java.util.UUID;

@Repository
public interface LoanRequestRepository extends JpaRepository<LoanRequest, UUID>, JpaSpecificationExecutor<LoanRequest> {
    //for some reason hibernate didn't map loan to loanRequest so i needed to write query
    @Query("SELECT lr FROM LoanRequest lr LEFT JOIN FETCH lr.loan")
    Page<LoanRequest> findAllWithLoans(Specification<LoanRequest> spec, Pageable pageable);

}
