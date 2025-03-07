package rs.banka4.user_service.domain.user.client.mapper;

import org.springframework.stereotype.Component;
import rs.banka4.user_service.domain.user.client.dtos.ClientDto;
import rs.banka4.user_service.domain.user.client.db.Client;

@Component
public class BasicClientMapperForGetAll {
    public ClientDto toDto(Client client) {
        if (client == null) {
            return null;
        }
        return new ClientDto(
                client.getId(),
                client.getFirstName(),
                client.getLastName(),
                client.getDateOfBirth(),
                client.getGender(),
                client.getEmail(),
                client.getPhone(),
                client.getAddress(),
                client.getPrivileges());
    }
}
