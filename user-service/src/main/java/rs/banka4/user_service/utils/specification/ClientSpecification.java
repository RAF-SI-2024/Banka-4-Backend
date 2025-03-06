package rs.banka4.user_service.utils.specification;

import org.springframework.data.jpa.domain.Specification;
import rs.banka4.user_service.models.Client;

public class ClientSpecification {
    public static Specification<Client> hasFirstName(String firstName) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%"
        );
    }

    public static Specification<Client> hasLastName(String lastName) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%"
        );
    }

    public static Specification<Client> hasEmail(String email) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.get("email")), "%" + email.toLowerCase() + "%"
        );
    }

    public static Specification<Client> hasLinkedAccount(String linkedAccount) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isMember(
                linkedAccount, root.get("linkedAccounts")
        );
    }
}
