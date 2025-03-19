package rs.banka4.user_service.utils.specification;

import org.springframework.data.jpa.domain.Specification;
import rs.banka4.user_service.domain.card.db.Card;

public class CardSpecification {

    public static Specification<Card> hasCardNumber(String cardNumber) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
            criteriaBuilder.lower(root.get("cardNumber")),
            "%" + cardNumber.toLowerCase() + "%"
        );
    }

    public static Specification<Card> hasFirstName(String firstName) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
            criteriaBuilder.lower(root.get("firstName")),
            "%" + firstName.toLowerCase() + "%"
        );
    }

    public static Specification<Card> hasLastName(String lastName) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
            criteriaBuilder.lower(root.get("lastName")),
            "%" + lastName.toLowerCase() + "%"
        );
    }

    public static Specification<Card> hasEmail(String email) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
            criteriaBuilder.lower(root.get("email")),
            "%" + email.toLowerCase() + "%"
        );
    }

    public static Specification<Card> hasCardStatus(String cardStatus) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
            criteriaBuilder.lower(root.get("cardStatus")),
            "%" + cardStatus.toLowerCase() + "%"
        );
    }
}
