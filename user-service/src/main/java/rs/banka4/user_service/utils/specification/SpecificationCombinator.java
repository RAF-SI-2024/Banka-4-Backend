package rs.banka4.user_service.utils.specification;

import org.springframework.data.jpa.domain.Specification;

public class SpecificationCombinator<T> {

    private Specification<T> specification;

    public SpecificationCombinator() {
        this.specification = Specification.where(null);
    }

    public SpecificationCombinator<T> and(Specification<T> other) {
        if (other != null) this.specification = this.specification.and(other);
        return this;
    }

    public Specification<T> build() {
        return this.specification;
    }
}
