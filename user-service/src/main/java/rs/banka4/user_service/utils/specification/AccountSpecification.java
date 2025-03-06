package rs.banka4.user_service.utils.specification;

import jakarta.persistence.criteria.Join;


import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import rs.banka4.user_service.models.Account;
import rs.banka4.user_service.models.Client;

public class AccountSpecification {

    public static Specification<Account> hasFirstName(String firstName) {
        return (root, query, criteriaBuilder) -> {
            Join<Account, Client> clientJoin = root.join("client", JoinType.INNER);

            return criteriaBuilder.like(
                    criteriaBuilder.lower(clientJoin.get("firstName")), "%" + firstName.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Account> hasLastName(String lastName) {
        return (root, query, criteriaBuilder) -> {
            Join<Account, Client> clientJoin = root.join("client", JoinType.INNER);

            return criteriaBuilder.like(
                    criteriaBuilder.lower(clientJoin.get("lastName")), "%" + lastName.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Account> hasEmail(String email) {
        return (root, query, criteriaBuilder) -> {
            Join<Account, Client> clientJoin = root.join("client", JoinType.INNER);

            return criteriaBuilder.like(
                    criteriaBuilder.lower(clientJoin.get("email")), "%" + email.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Account> hasAccountNumber(String accountNumber) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.get("accountNumber")), "%" + accountNumber.toLowerCase() + "%"
        );
    }
}