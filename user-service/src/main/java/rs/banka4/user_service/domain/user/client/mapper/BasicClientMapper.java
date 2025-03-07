package rs.banka4.user_service.domain.user.client.mapper;

import org.springframework.stereotype.Component;
import rs.banka4.user_service.domain.user.client.dtos.ClientDto;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.domain.user.Privilege;

import java.util.EnumSet;

@Component
public class BasicClientMapper {
    public ClientDto entityToDto(Client client) {
        if (client == null) return null;
        EnumSet<Privilege> set = EnumSet.noneOf(Privilege.class);
        ClientDto dto = new ClientDto(
                client.id,
                client.firstName,
                client.lastName,
                client.dateOfBirth,
                client.gender,
                client.email,
                client.phone,
                client.address,
                set);
        return dto;
    }
}
