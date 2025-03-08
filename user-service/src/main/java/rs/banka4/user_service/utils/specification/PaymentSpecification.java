package rs.banka4.user_service.utils.specification;

import org.springframework.data.jpa.domain.Specification;
import rs.banka4.user_service.domain.transaction.db.TransactionStatus;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.transaction.db.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PaymentSpecification {

    public static Specification<Transaction> hasStatus(TransactionStatus status) {
        return (root, query, criteriaBuilder) ->
                status == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Transaction> hasAmount(BigDecimal amount) {
        return (root, query, criteriaBuilder) -> {
            if (amount == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.join("from").get("amount"), amount);
        };
    }

    public static Specification<Transaction> hasDate(LocalDate date) {
        return (root, query, criteriaBuilder) ->
                date == null ? criteriaBuilder.conjunction() : criteriaBuilder.between(root.get("paymentDateTime"), date.atStartOfDay(), date.plusDays(1).atStartOfDay());
    }

    public static Specification<Transaction> hasFromAccount(Account fromAccount) {
        return (root, query, criteriaBuilder) ->
                fromAccount == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("fromAccount"), fromAccount);
    }

    public static Specification<Transaction> hasToAccount(Account toAccount) {
        return (root, query, criteriaBuilder) ->
                toAccount == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("toAccount"), toAccount);
    }
}