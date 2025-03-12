package rs.banka4.user_service.repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.banka4.user_service.domain.loan.db.InterestRate;

@Repository
public interface InterestRateRepository extends JpaRepository<InterestRate, UUID> {
    @Query(
        "SELECT i FROM InterestRate i WHERE :loanAmount BETWEEN i.minAmount AND i.maxAmount "
            + "AND :date BETWEEN i.dateActiveFrom AND i.dateActiveTo"
    )
    Optional<InterestRate> findByAmountAndDate(
        @Param("loanAmount") BigDecimal loanAmount,
        @Param("date") LocalDate date
    );
}
