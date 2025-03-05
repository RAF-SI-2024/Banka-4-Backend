package rs.banka4.user_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.banka4.user_service.models.AuthenticationEvent;
import java.util.UUID;

@Repository
public interface AuthenticationEventRepository extends JpaRepository<AuthenticationEvent, UUID> {
}
