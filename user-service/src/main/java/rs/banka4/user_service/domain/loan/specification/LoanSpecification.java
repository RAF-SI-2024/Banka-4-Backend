package rs.banka4.user_service.domain.loan.specification;

import jakarta.persistence.ManyToOne;
import jakarta.persistence.criteria.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.loan.db.*;
import rs.banka4.user_service.domain.loan.dtos.LoanFilterDto;

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
                if (
                    filter.status()
                        .equals(LoanStatus.PROCESSING)
                ) predicates.add(builder.notEqual(root.get("status"), filter.status()));
                else predicates.add(builder.equal(root.get("status"), filter.status()));
            }

            if (filter.status() == null) {
                predicates.add(builder.notEqual(root.get("status"), LoanStatus.PROCESSING));
            }

            if (
                filter.accountNumber() != null
                    && !filter.accountNumber()
                        .isEmpty()
            ) {
                Join<Loan, Account> accountJoin = root.join("account");
                predicates.add(
                    builder.equal(accountJoin.get("accountNumber"), filter.accountNumber())
                );
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

            if (root.get("loan") != null) {
                Join<Loan, Account> accountJoin = root.join("loan");
                predicates.add(builder.equal(accountJoin.get("status"), LoanStatus.PROCESSING));
            }

            if (filter.type() != null) {
                predicates.add(builder.equal(root.get("type"), filter.type()));
            }

            if (
                filter.accountNumber() != null
                    && !filter.accountNumber()
                        .isEmpty()
            ) {
                Join<Loan, Account> accountJoin = root.join("account");
                predicates.add(
                    builder.equal(accountJoin.get("accountNumber"), filter.accountNumber())
                );
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<LoanInstallment> findPaidAndNextUpcomingInstallment(UUID loanId) {
        return (root, query, builder) -> {
            Predicate loanPredicate =
                builder.equal(
                    root.get("loan")
                        .get("id"),
                    loanId
                );

            Predicate paidPredicate = builder.equal(root.get("paymentStatus"), PaymentStatus.PAID);

            Subquery<LocalDate> subquery = query.subquery(LocalDate.class);
            Root<LoanInstallment> subRoot = subquery.from(LoanInstallment.class);
            subquery.select(builder.least(subRoot.<LocalDate>get("expectedDueDate")))
                .where(
                    builder.and(
                        builder.equal(
                            subRoot.get("loan")
                                .get("id"),
                            loanId
                        ),
                        subRoot.get("paymentStatus")
                            .in(PaymentStatus.UNPAID, PaymentStatus.DELAYED)
                    )
                );

            Predicate upcomingPredicate =
                builder.and(
                    root.get("paymentStatus")
                        .in(PaymentStatus.UNPAID, PaymentStatus.DELAYED),
                    builder.equal(root.get("expectedDueDate"), subquery)
                );

            Predicate combinedPredicate = builder.or(paidPredicate, upcomingPredicate);

            return builder.and(loanPredicate, combinedPredicate);
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
