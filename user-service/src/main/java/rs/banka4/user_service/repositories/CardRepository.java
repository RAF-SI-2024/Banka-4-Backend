package rs.banka4.user_service.repositories;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.banka4.user_service.domain.card.db.Card;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {
    Optional<Card> findCardByCardNumber(String cardNumber);

    boolean existsByCardNumber(String cardNumber);

}
