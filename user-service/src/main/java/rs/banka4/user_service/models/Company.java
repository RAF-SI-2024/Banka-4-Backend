package rs.banka4.user_service.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String tin; // PIB

    @Column(nullable = false, unique = true)
    private String crn; // Maticni Broj

    @Column(nullable = false)
    private String address;

    @ManyToOne
    private ActivityCode activityCode;

    @ManyToOne
    private Client majorityOwner;

}
