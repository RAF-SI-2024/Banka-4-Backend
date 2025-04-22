package rs.banka4.bank_service.domain.listing.specificaion;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import rs.banka4.bank_service.domain.exchanges.db.Exchange_;
import rs.banka4.bank_service.domain.listing.db.Listing;
import rs.banka4.bank_service.domain.listing.db.Listing_;
import rs.banka4.bank_service.domain.listing.dtos.ListingFilterDto;
import rs.banka4.bank_service.domain.listing.dtos.SecurityType;
import rs.banka4.bank_service.domain.options.db.Asset_;
import rs.banka4.bank_service.domain.orders.db.Order;
import rs.banka4.bank_service.domain.orders.db.Order_;
import rs.banka4.bank_service.domain.security.Security_;
import rs.banka4.bank_service.domain.security.future.db.Future;
import rs.banka4.bank_service.domain.security.future.db.Future_;
import rs.banka4.bank_service.domain.security.stock.db.Stock;

public class ListingSpecification {
    /**
     * Builds a JPA {@link Specification} for filtering and sorting {@link Listing} entities based
     * on the provided filter criteria and client role.
     * <p>
     * This specification performs the following:
     * <ul>
     * <li>Ensures that only the latest {@code Listing} per security is returned via the
     * {@code isLatest()} predicate.</li>
     * <li>Filters by security type (for clients, only FUTURE and STOCK are allowed; for employees,
     * the specified type is applied).</li>
     * <li>Performs "like" search on the security's name and ticker.</li>
     * <li>Filters by exchange prefix.</li>
     * <li>Applies range filters on ask and bid prices.</li>
     * <li>If the security type is {@code FUTURE}, applies range filtering on the settlement
     * date.</li>
     * <li>Computes volume for each listing by counting {@link Order} entities created for the
     * security during the current day and applies volume range filters.</li>
     * <li>Applies dynamic sorting based on the {@code sortBy} and {@code sortDirection} parameters.
     * For instance, sorting by price will use the Listing's {@code ask} value, and sorting by
     * volume will use the computed volume from the subquery.</li>
     * </ul>
     * </p>
     *
     * @param filter the {@link ListingFilterDto} containing filter, search, and sorting parameters;
     *        may include search text, price ranges, exchange prefix, settlement date range, volume
     *        range, and sort options.
     * @param isClient {@code true} if the filtering should restrict the results to only
     *        {@code STOCK} and {@code FUTURE} security types (for client users); {@code false} if
     *        no such restriction should be applied.
     * @return a {@link Specification} that can be used with Spring Data JPA repositories to query
     *         {@link Listing} entities based on the provided criteria.
     */
    public static Specification<Listing> getSpecification(
        ListingFilterDto filter,
        boolean isClient
    ) {
        return isLatest().and((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            var secJoin = root.join(Listing_.security);
            var exchangesJoin = root.join(Listing_.exchange);
            if (isClient) {
                if (
                    filter.getSecurityType() != null
                        && List.of(SecurityType.FUTURE, SecurityType.STOCK)
                            .contains(filter.getSecurityType())
                ) {
                    predicates.add(
                        cb.equal(
                            secJoin.type(),
                            filter.getSecurityType()
                                .getTypeClass()
                        )
                    );
                } else {
                    var p = cb.equal(secJoin.type(), cb.literal(Future.class));
                    var p2 = cb.equal(secJoin.type(), cb.literal(Stock.class));
                    predicates.add(cb.or(p, p2));
                }

            } else {
                if (filter.getSecurityType() != null) {
                    predicates.add(
                        cb.equal(
                            secJoin.type(),
                            filter.getSecurityType()
                                .getTypeClass()
                        )
                    );
                }
            }
            if (
                filter.getSearchName() != null
                    && !filter.getSearchName()
                        .isBlank()
            ) {
                predicates.add(
                    cb.like(
                        cb.lower(secJoin.get(Security_.name)),
                        "%"
                            + filter.getSearchName()
                                .toLowerCase()
                            + "%"
                    )
                );
            }
            if (
                filter.getSearchTicker() != null
                    && !filter.getSearchTicker()
                        .isBlank()
            ) {
                predicates.add(
                    cb.like(
                        cb.lower(secJoin.get(Security_.ticker)),
                        "%"
                            + filter.getSearchTicker()
                                .toLowerCase()
                            + "%"
                    )
                );
            }

            if (
                filter.getExchangePrefix() != null
                    && !filter.getExchangePrefix()
                        .isBlank()
            ) {
                predicates.add(
                    cb.like(
                        cb.lower(exchangesJoin.get(Exchange_.exchangeName)),
                        filter.getExchangePrefix()
                            .toLowerCase()
                            + "%"
                    )
                );
            }
            if (filter.getAskMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get(Listing_.ask), filter.getAskMin()));
            }
            if (filter.getAskMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get(Listing_.ask), filter.getAskMax()));
            }
            if (filter.getBidMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get(Listing_.bid), filter.getBidMin()));
            }
            if (filter.getBidMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get(Listing_.bid), filter.getBidMax()));
            }
            if (SecurityType.FUTURE.equals(filter.getSecurityType())) {
                if (
                    filter.getSettlementDateFrom() != null || filter.getSettlementDateTo() != null
                ) {
                    if (
                        filter.getSettlementDateFrom() != null
                            && filter.getSettlementDateTo() != null
                    ) {
                        predicates.add(
                            cb.between(
                                secJoin.get(Future_.SETTLEMENT_DATE),
                                filter.getSettlementDateFrom(),
                                filter.getSettlementDateTo()
                            )
                        );
                    } else if (filter.getSettlementDateFrom() != null) {
                        predicates.add(
                            cb.greaterThanOrEqualTo(
                                secJoin.get(Future_.SETTLEMENT_DATE),
                                filter.getSettlementDateFrom()
                            )
                        );
                    } else {
                        predicates.add(
                            cb.lessThanOrEqualTo(
                                secJoin.get(Future_.SETTLEMENT_DATE),
                                filter.getSettlementDateTo()
                            )
                        );
                    }
                }
            }
            OffsetDateTime now = OffsetDateTime.now();
            OffsetDateTime start = now.truncatedTo(ChronoUnit.DAYS);
            OffsetDateTime end = start.plusDays(1);

            Subquery<Long> volumeSubquery = query.subquery(Long.class);
            Root<Order> orderRoot = volumeSubquery.from(Order.class);
            volumeSubquery.select(cb.count(orderRoot));
            volumeSubquery.where(
                cb.equal(
                    orderRoot.get(Order_.asset)
                        .get(Asset_.id),
                    root.get(Listing_.security)
                        .get(Security_.id)
                ),
                cb.between(orderRoot.get(Order_.createdAt), start, end)
            );

            if (filter.getVolumeMin() != null || filter.getVolumeMax() != null) {

                if (filter.getVolumeMin() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(volumeSubquery, filter.getVolumeMin()));
                }
                if (filter.getVolumeMax() != null) {
                    predicates.add(cb.lessThanOrEqualTo(volumeSubquery, filter.getVolumeMax()));
                }
            }
            if (filter.getSortBy() != null && filter.getSortDirection() != null) {
                switch (filter.getSortBy()) {
                case PRICE:
                    if (
                        filter.getSortDirection()
                            .equals(ListingFilterDto.SortDirection.ASC)
                    ) {
                        query.orderBy(cb.asc(root.get(Listing_.ask)));
                    } else {
                        query.orderBy(cb.desc(root.get(Listing_.ask)));
                    }
                    break;
                case VOLUME:
                    if (
                        filter.getSortDirection()
                            .equals(ListingFilterDto.SortDirection.ASC)
                    ) {
                        query.orderBy(cb.asc(volumeSubquery));
                    } else {
                        query.orderBy(cb.desc(volumeSubquery));
                    }
                    break;
                default:
                    break;
                }
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        });
    }

    /**
     * For a given listing, accept it only if it is the latest for its security
     */
    static Specification<Listing> isLatest() {
        return ((root, query, cb) -> {
            final var subquery = query.subquery(OffsetDateTime.class);
            final var subRoot = subquery.from(Listing.class);

            return root.get(Listing_.lastRefresh)
                .in(
                    subquery.select(cb.greatest(subRoot.get(Listing_.lastRefresh)))
                        .groupBy(
                            subRoot.get(Listing_.security)
                                .get(Security_.id)
                        )
                );
        });
    }

}
