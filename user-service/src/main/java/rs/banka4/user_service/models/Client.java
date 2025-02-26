package rs.banka4.user_service.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

@Entity(name = "clients")
@Data
@Validated
@NoArgsConstructor
@AllArgsConstructor
@With
public class Client extends User {

    @ElementCollection
    @CollectionTable(name = "client_linked_accounts", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "linked_account")
    private Set<String> linkedAccounts;
}