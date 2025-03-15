package rs.banka4.user_service.domain.card.db;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.ToString;
import rs.banka4.user_service.domain.user.Gender;

import java.time.LocalDate;
import java.util.UUID;

@Embeddable
@Builder
public record AuthorizedUser(
        UUID userId,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String email,
        String phoneNumber,
        String address,
        @Enumerated(EnumType.STRING)
        Gender gender
) { }
