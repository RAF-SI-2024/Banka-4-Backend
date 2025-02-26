package rs.banka4.user_service.utils.specification;

import org.springframework.data.jpa.domain.Specification;
import rs.banka4.user_service.models.Employee;

public class EmployeeSpecification {

    public static Specification<Employee> hasFirstName(String firstName) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("firstName"), firstName);
    }

    public static Specification<Employee> hasLastName(String lastName) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("lastName"), lastName);
    }

    public static Specification<Employee> hasEmail(String email) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("email"), email);
    }

    public static Specification<Employee> hasPosition(String position) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("position"), position);
    }

}
