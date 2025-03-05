package rs.banka4.user_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.banka4.user_service.models.Company;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {
    boolean existsByTin(String s);
    boolean existsByCrn(String crn);
    boolean existsByName(String name);

    Optional<Company> findByCrn(String crn);
}
