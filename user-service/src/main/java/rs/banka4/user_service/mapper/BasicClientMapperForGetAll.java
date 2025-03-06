package rs.banka4.user_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.springframework.stereotype.Component;
import rs.banka4.user_service.dto.ClientDto;
import rs.banka4.user_service.models.Client;

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
