package rs.banka4.user_service.domain.loan.db;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "interest_rates")
public class InterestRate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "min_amount", nullable = false)
    private BigDecimal minAmount;

    @Column(name = "max_amount", nullable = false)
    private BigDecimal maxAmount;

    @Column(name = "fixed_rate", nullable = false)
    private BigDecimal fixedRate;

    @Column(name = "date_active_from", nullable = false)
    private LocalDate dateActiveFrom;

    @Column(name = "date_active_to", nullable = false)
    private LocalDate dateActiveTo;


    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "InterestRate{" +
                "id=" + id +
                ", minAmount=" + minAmount +
                ", maxAmount=" + maxAmount +
                ", fixedRate=" + fixedRate +
                ", dateActiveFrom=" + dateActiveFrom +
                ", dateActiveTo=" + dateActiveTo +
                '}';
    }
}