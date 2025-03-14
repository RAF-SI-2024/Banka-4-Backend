package rs.banka4.user_service.utils.specification;

import jakarta.persistence.ManyToOne;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.loan.db.Loan;

public class LoanSpecification {

    @ManyToOne
    private Account account;

    public static Specification<Loan> hasAccountNumber(String accountNumber) {
        return (root, query, criteriaBuilder) -> {
            Join<Loan, Account> accountJoin = root.join("account", JoinType.INNER);
            return criteriaBuilder.equal(
                    criteriaBuilder.lower(accountJoin.get("accountNumber")),
                    accountNumber
            );
        };
    }

}
