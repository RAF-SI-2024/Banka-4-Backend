package rs.banka4.user_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.banka4.user_service.domain.loan.db.LoanInstallment;
import rs.banka4.user_service.domain.loan.db.PaymentStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface LoanInstallmentRepository extends JpaRepository<rs.banka4.user_service.domain.loan.db.LoanInstallment, UUID> {
    List<LoanInstallment> findByExpectedDueDateAndPaymentStatus(LocalDate date, PaymentStatus status);
    List<LoanInstallment> findByPaymentStatusAndExpectedDueDate(PaymentStatus status, LocalDate date);
    List<LoanInstallment> findByPaymentStatusAndExpectedDueDateIsLessThan(PaymentStatus status, LocalDate date);

    @Query("SELECT li FROM LoanInstallment li WHERE li.paymentStatus = :status AND li.expectedDueDate > :threshold")
    List<LoanInstallment> findRecentDelayedInstallments(
            @Param("status") PaymentStatus status,
            @Param("threshold") LocalDate threshold);
}
