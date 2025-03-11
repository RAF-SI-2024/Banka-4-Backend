package rs.banka4.user_service.domain.card.db;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import rs.banka4.user_service.domain.user.Gender;

import java.time.LocalDate;
import java.util.UUID;

@Embeddable
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
) {
        public static Builder builder() {
                return new Builder();
        }

        public static class Builder {
                private UUID userId;
                private String firstName;
                private String lastName;
                private LocalDate dateOfBirth;
                private String email;
                private String phoneNumber;
                private String address;
                private Gender gender;

                public Builder userId(UUID userId) {
                        this.userId = userId;
                        return this;
                }

                public Builder firstName(String firstName) {
                        this.firstName = firstName;
                        return this;
                }

                public Builder lastName(String lastName) {
                        this.lastName = lastName;
                        return this;
                }

                public Builder dateOfBirth(LocalDate dateOfBirth) {
                        this.dateOfBirth = dateOfBirth;
                        return this;
                }

                public Builder email(String email) {
                        this.email = email;
                        return this;
                }

                public Builder phoneNumber(String phoneNumber) {
                        this.phoneNumber = phoneNumber;
                        return this;
                }

                public Builder address(String address) {
                        this.address = address;
                        return this;
                }

                public Builder gender(Gender gender) {
                        this.gender = gender;
                        return this;
                }

                public AuthorizedUser build() {
                        return new AuthorizedUser(userId, firstName, lastName, dateOfBirth, email, phoneNumber, address, gender);
                }
        }
}
