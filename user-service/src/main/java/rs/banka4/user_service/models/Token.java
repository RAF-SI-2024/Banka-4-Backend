package rs.banka4.user_service.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Entity(name = "tokens")
@Data
@Validated
@NoArgsConstructor
@AllArgsConstructor
@With
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true)
    private String token;

    @Column
    public boolean valid;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

}
