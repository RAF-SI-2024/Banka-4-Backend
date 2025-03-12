package rs.banka4.user_service.repositories;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import rs.banka4.user_service.domain.user.client.db.Client;

@Repository
public interface ClientRepository extends
    JpaRepository<Client, UUID>,
    JpaSpecificationExecutor<Client> {

    Optional<Client> findByEmail(String email);

    boolean existsByEmail(String email);
}
