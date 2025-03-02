package rs.banka4.user_service.generator;

import rs.banka4.user_service.dto.ClientDto;
import rs.banka4.user_service.models.Privilege;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

public class ClientObjectMother {
    public static ClientDto generateExampleClientDto() {
        return new ClientDto(
                "1fad2c01-f82f-41a6-822c-8ca1b3232575",
                "Mehmedalija",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "Male",
                "danny.jo@example.com",
                "123-456-7890",
                "123 Main St",
                EnumSet.noneOf(Privilege.class),
                Set.of("acc1", "acc2", "acc3")
        );
    }
}
