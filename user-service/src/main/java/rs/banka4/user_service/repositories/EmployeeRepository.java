package rs.banka4.user_service.repositories;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import rs.banka4.user_service.domain.user.employee.db.Employee;

@Repository
public interface EmployeeRepository extends
    JpaRepository<Employee, UUID>,
    JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<Employee> findByUsername(String username);
}
