package rs.banka4.user_service.repositories;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.company.db.Company;
import rs.banka4.user_service.domain.user.client.db.Client;

@Repository
public interface AccountRepository extends
    JpaRepository<Account, UUID>,
    JpaSpecificationExecutor<Account> {
    Optional<Account> findAccountByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    Set<Account> findAllByClient(Client client);

    Optional<Account> getAccountByAccountNumber(String accountNumber);

    List<Account> findAllByCompany(@Param("company") Company company);
}
