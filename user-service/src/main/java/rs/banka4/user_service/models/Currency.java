package rs.banka4.user_service.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "currencies")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Currency {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private boolean active;

    @Enumerated(EnumType.STRING)
    private Code code;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "currency_countries", joinColumns = @JoinColumn(name = "currency_id"))
    @Column(name = "country")
    @Builder.Default
    private Set<String> countries = new HashSet<>();

    public enum Code {
        RSD, EUR, USD, CHF, JPI, AUD, CAD
    }

}
