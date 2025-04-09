package rs.banka4.stock_service.utils.specification;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import rs.banka4.stock_service.domain.actuaries.db.ActuaryInfo;

public class ActuaryInfoSpecification {

    public static Specification<ActuaryInfo> hasUserId(UUID userId) {
        return (root, query, cb) -> cb.equal(root.get("userId"), userId);
    }

    public static Specification<ActuaryInfo> needsApproval(boolean value) {
        return (root, query, cb) -> cb.equal(root.get("needApproval"), value);
    }

    public static Specification<ActuaryInfo> limitGreaterThan(BigDecimal amount) {
        return (root, query, cb) -> cb.greaterThan(root.get("limitAmount"), amount);
    }

    public static Specification<ActuaryInfo> usedLimitLessThan(BigDecimal amount) {
        return (root, query, cb) -> cb.lessThan(root.get("usedLimitAmount"), amount);
    }
}
