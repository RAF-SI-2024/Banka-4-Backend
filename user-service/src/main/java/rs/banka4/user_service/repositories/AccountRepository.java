package rs.banka4.user_service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import rs.banka4.user_service.models.Account;
import rs.banka4.user_service.models.Client;
import rs.banka4.user_service.models.Currency;

import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID>, JpaSpecificationExecutor<Account> {
    Page<Account> findAllByCurrency_Code(Currency.Code currency, PageRequest pageRequest);
    Page<Account> findAllByCurrency_CodeNot(Currency.Code currency, PageRequest pageRequest);

    Page<Account> findAllByClientAndCurrency_Code(Client client, Currency.Code currency, PageRequest pageRequest);
    Page<Account> findAllByClientAndCurrency_CodeNot(Client client, Currency.Code currency, PageRequest pageRequest);

    Page<Account> findAllByClient(Client client, PageRequest pageRequest);

}
