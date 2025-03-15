package rs.banka4.user_service.domain.loan.specification;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.loan.db.Loan;
import rs.banka4.user_service.domain.loan.db.LoanRequest;
import rs.banka4.user_service.domain.loan.db.LoanStatus;
import rs.banka4.user_service.domain.loan.dtos.LoanFilterDto;

import java.util.ArrayList;
import java.util.List;

public class LoanSpecification {

    @ManyToOne
    private Account account;

    public static Specification<Loan> searchLoans(LoanFilterDto filter) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.type() != null) {
                predicates.add(builder.equal(root.get("type"), filter.type()));
            }

            if (filter.status() != null) {
                if(filter.status().equals(LoanStatus.PROCESSING))
                    predicates.add(builder.notEqual(root.get("status"), filter.status()));
                else
                    predicates.add(builder.equal(root.get("status"), filter.status()));
            }

            if (filter.status() == null) {
                predicates.add(builder.notEqual(root.get("status"), LoanStatus.PROCESSING));
            }

            if (filter.accountNumber() != null && !filter.accountNumber().isEmpty()) {
                Join<Loan, Account> accountJoin = root.join("account");
                predicates.add(builder.equal(accountJoin.get("accountNumber"), filter.accountNumber()));
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<LoanRequest> searchLoanRequests(LoanFilterDto filter) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query.getResultType() != Long.class) {
                root.fetch("loan", JoinType.LEFT);
            }

            if (filter.type() != null) {
                predicates.add(builder.equal(root.get("type"), filter.type()));
            }

            if (filter.accountNumber() != null && !filter.accountNumber().isEmpty()) {
                Join<Loan, Account> accountJoin = root.join("account");
                predicates.add(builder.equal(accountJoin.get("accountNumber"), filter.accountNumber()));
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

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