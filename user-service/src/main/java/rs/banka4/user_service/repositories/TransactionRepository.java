package rs.banka4.user_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.banka4.user_service.models.Transaction;

import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
}
