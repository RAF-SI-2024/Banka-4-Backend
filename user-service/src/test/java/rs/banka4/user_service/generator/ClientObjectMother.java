package rs.banka4.user_service.generator;

import rs.banka4.user_service.domain.user.Privilege;
import rs.banka4.user_service.domain.user.User;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.domain.user.client.dtos.ClientDto;
import rs.banka4.user_service.domain.user.client.dtos.CreateClientDto;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

public class ClientObjectMother {

    public static CreateClientDto createClientDto() {
        return new CreateClientDto(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "Male",
                "john.doe@example.com",
                "1234567890",
                "123 Grove Street, City, Country",
                Set.of(Privilege.SEARCH)
        );
    }

    public static Client.ClientBuilder<?, ?> client() {
        return Client.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender(User.Gender.MALE)
                .email("john.doe@example.com")
                .phone("123-456-7890")
                .address("123 Main St");
    }

    public static ClientDto generateClientDto(UUID id, String email) {
        String firstName = "John";
        String lastName = "Doe";
        LocalDate dateOfBirth = LocalDate.of(1990, 1, 1);
        User.Gender gender = User.Gender.MALE;
        String phone = "123-456-7890";
        String address = "123 Main St";
        EnumSet<Privilege> privileges  = EnumSet.noneOf(Privilege.class);

        return new ClientDto(id, firstName, lastName, dateOfBirth, gender, email, phone, address, privileges);
    }

    public static Client generateClient(UUID id, String email) {
        return Client.builder()
                .id(id)
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender(User.Gender.MALE)
                .email(email)
                .phone("123-456-7890")
                .address("123 Main St")
                .build();
    }
}