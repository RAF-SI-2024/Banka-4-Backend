package rs.banka4.user_service.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.card.db.Card;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID>, JpaSpecificationExecutor<Card> {
    int countByAccount(Account account);

    int countByAccountAndAuthorizedUserEmail(Account account, String authorizedUserEmail);

    boolean existsByAccountAndAuthorizedUserEmail(Account account, String email);

    boolean existsByCardNumber(String cardNumber);

    Optional<Card> findCardByCardNumber(String cardNumber);

    List<Card> findByAccountAccountNumber(String accountNumber);

}
