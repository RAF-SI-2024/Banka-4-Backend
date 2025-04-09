package rs.banka4.user_service.service.mock.generators;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import rs.banka4.rafeisen.common.dto.Gender;
import rs.banka4.rafeisen.common.security.Privilege;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.domain.user.client.dtos.*;

public class ClientObjectMother {

    public static CreateClientDto generateBasicCreateClientDto() {
        return new CreateClientDto(
            "John",
            "Doe",
            LocalDate.of(1990, 1, 1),
            Gender.MALE,
            "john.doe@example.com",
            "1234567890",
            "123 Grove Street, City, Country",
            Set.of()
        );
    }

    public static ClientDto generateBasicClientDto(UUID id, String email) {
        String firstName = "John";
        String lastName = "Doe";
        LocalDate dateOfBirth = LocalDate.of(1990, 1, 1);
        Gender gender = Gender.MALE;
        String phone = "+381642587413";
        String address = "123 Main St";
        EnumSet<Privilege> privileges = EnumSet.noneOf(Privilege.class);

        return new ClientDto(
            id,
            firstName,
            lastName,
            dateOfBirth,
            gender,
            email,
            phone,
            address,
            privileges,
            false
        );
    }

    public static ClientDto generateBasicClientDto() {
        UUID id = UUID.randomUUID();
        return new ClientDto(
            id,
            "John",
            "Doe",
            LocalDate.of(1990, 1, 1),
            Gender.MALE,
            "john.doe@example.com",
            "+381621403658",
            "123 Main St",
            EnumSet.noneOf(Privilege.class),
            false
        );
    }

    public static UpdateClientDto generateBasicUpdateClientDto() {
        return new UpdateClientDto(
            "John",
            "Doe",
            LocalDate.of(1990, 1, 1),
            Gender.MALE,
            "john.doe@example.com",
            "+381630124756",
            "123 Grove Street, City, Country",
            Set.of()
        );
    }

    public static Client generateClient(UUID id, String email) {
        return Client.builder()
            .id(id)
            .firstName("John")
            .lastName("Doe")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .gender(Gender.MALE)
            .email(email)
            .phone("+381646325014")
            .address("123 Main St")
            .build();
    }

    public static ClientContactDto generateBasicClientContactDto() {
        return new ClientContactDto(
            UUID.randomUUID(),
            "Wasketov racun od firme",
            "444394438340549"
        );
    }

    public static ClientContactRequest generateBasicClientContactRequest() {
        return new ClientContactRequest("Wasketov racun od firme", "444394438340549");
    }

}
