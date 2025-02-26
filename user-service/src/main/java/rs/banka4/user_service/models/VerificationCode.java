package rs.banka4.user_service.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.With;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

@Getter
@Entity(name = "verification_tokens")
@Data
@Validated
@AllArgsConstructor
@With
public class VerificationCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;
    private LocalDateTime expirationDate;
    private boolean used;
    private String email;

    public VerificationCode() {}

    public VerificationCode(String code, LocalDateTime expirationDate, String email) {
        this.code = code;
        this.expirationDate = expirationDate;
        this.email = email;
        this.used = false;
    }

}
