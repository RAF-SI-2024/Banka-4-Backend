package rs.banka4.user_service.domain.card.db;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;
import rs.banka4.user_service.domain.account.db.Account;

@Entity
@AllArgsConstructor
@Getter
@Setter
@RequiredArgsConstructor
@Builder
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(
        nullable = false,
        unique = true
    )
    @Pattern(regexp = "\\d{16}")
    private String cardNumber;

    @Column(nullable = false)
    private String cvv;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardName cardName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CardType cardType = CardType.DEBIT;

    @Column(name = "card_limit")
    @Builder.Default
    private BigDecimal limit = BigDecimal.valueOf(10000);

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CardStatus cardStatus = CardStatus.ACTIVATED;

    @ManyToOne
    private Account account;

    @Embedded
    private AuthorizedUser authorizedUser;

    @Builder.Default
    private LocalDate createdAt = LocalDate.now();

    @Builder.Default
    private LocalDate expiresAt =
        LocalDate.now()
            .plusYears(5);

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass =
            o instanceof HibernateProxy
                ? ((HibernateProxy) o).getHibernateLazyInitializer()
                    .getPersistentClass()
                : o.getClass();
        Class<?> thisEffectiveClass =
            this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer()
                    .getPersistentClass()
                : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Card card = (Card) o;
        return getId() != null && Objects.equals(getId(), card.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer()
                .getPersistentClass()
                .hashCode()
            : getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Card{" + "id=" + id + ", cardNumber='" + cardNumber + '\'' + '}';
    }
}
