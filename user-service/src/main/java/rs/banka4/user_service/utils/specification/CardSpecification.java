package rs.banka4.user_service.utils.specification;

import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.card.db.Card;
import rs.banka4.user_service.domain.user.client.db.Client;

public class CardSpecification {

    public static Specification<Card> hasCardNumber(String cardNumber) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("cardNumber"), cardNumber);
    }

    public static Specification<Card> hasCardStatus(String status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("cardStatus"), status);
    }

    public static Specification<Card> hasFirstName(String firstName) {
        return (root, query, criteriaBuilder) -> {
            Join<Card, Account> accountJoin = root.join("account");
            Join<Account, Client> clientJoin = accountJoin.join("client");

            return criteriaBuilder.or(
                    criteriaBuilder.equal(clientJoin.get("firstName"), firstName),
                    criteriaBuilder.equal(root.get("authorizedUser").get("firstName"), firstName)
            );
        };
    }

    public static Specification<Card> hasLastName(String lastName) {
        return (root, query, criteriaBuilder) -> {
            Join<Card, Account> accountJoin = root.join("account");
            Join<Account, Client> clientJoin = accountJoin.join("client");

            return criteriaBuilder.or(
                    criteriaBuilder.equal(clientJoin.get("lastName"), lastName),
                    criteriaBuilder.equal(root.get("authorizedUser").get("lastName"), lastName)
            );
        };
    }

    public static Specification<Card> hasEmail(String email) {
        return (root, query, criteriaBuilder) -> {
            Join<Card, Account> accountJoin = root.join("account");
            Join<Account, Client> clientJoin = accountJoin.join("client");

            return criteriaBuilder.or(
                    criteriaBuilder.equal(clientJoin.get("email"), email),
                    criteriaBuilder.equal(root.get("authorizedUser").get("email"), email)
            );
        };
    }
}
