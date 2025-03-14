package rs.banka4.user_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import rs.banka4.user_service.domain.loan.db.Loan;
import rs.banka4.user_service.domain.loan.db.LoanStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoanRepository extends JpaRepository<Loan, UUID>, JpaSpecificationExecutor<Loan> {
    Optional<Loan> findByLoanNumber(Long loanNum);

    Optional<Loan> findLoanByLoanNumber(Long loanNumber);
    Optional<List<Loan>> findByInterestTypeAndStatus(Loan.InterestType interestType, LoanStatus status);
}
