package rs.banka4.user_service.domain.card.db;

import jakarta.persistence.Embeddable;
import rs.banka4.user_service.domain.user.Gender;

import java.time.LocalDate;
import java.util.UUID;

@Embeddable
public record AuthorizedUser(
        UUID id,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String email,
        String phoneNumber,
        String address,
        Gender gender
) { }
