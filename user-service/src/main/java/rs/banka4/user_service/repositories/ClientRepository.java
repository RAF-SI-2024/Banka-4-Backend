package rs.banka4.user_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.banka4.user_service.models.Client;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, String> {
    Optional<Client> findByEmail(String email);
    boolean existsByEmail(String email);

    @Modifying
    @Query("DELETE FROM Client c JOIN c.savedContacts a WHERE c.id = :clientId AND a.id = :accountId")
    void deleteContactFromClient(@Param("clientId") String clientId, @Param("accountId") String accountId);

}
