package rs.banka4.user_service.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

@Entity(name = "clients")
@Data
@Validated
@NoArgsConstructor
@AllArgsConstructor
@With
public class Client extends User {

    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY)
    private Set<Account> accounts;

    @ManyToMany
    @JoinTable(
            name = "client_contacts",
            joinColumns = @JoinColumn(name = "client_id"),
            inverseJoinColumns = @JoinColumn(name = "contact_id")
    )
    private Set<Client> contacts;

}