package rs.banka4.user_service.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.banka4.rafeisen.common.currency.CurrencyCode;
import rs.banka4.user_service.domain.currency.db.Currency;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, UUID> {

    Currency findByCode(CurrencyCode.Code code);
}
