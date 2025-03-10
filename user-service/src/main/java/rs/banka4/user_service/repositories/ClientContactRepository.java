package rs.banka4.user_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.banka4.user_service.domain.user.client.db.ClientContact;

import java.util.UUID;

@Repository
public interface ClientContactRepository extends JpaRepository<ClientContact, UUID>  {

}
