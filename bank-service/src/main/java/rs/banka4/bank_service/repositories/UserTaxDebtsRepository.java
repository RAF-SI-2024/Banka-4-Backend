package rs.banka4.bank_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.banka4.bank_service.domain.taxes.db.UserTaxDebts;

import java.util.List;
import java.util.UUID;

public interface UserTaxDebtsRepository extends JpaRepository<UserTaxDebts, UUID> {
    List<UserTaxDebts> findByAccount_Client_Id(UUID userId);
}
