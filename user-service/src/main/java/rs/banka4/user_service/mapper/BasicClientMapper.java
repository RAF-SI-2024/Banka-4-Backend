package rs.banka4.user_service.mapper;

import org.springframework.stereotype.Component;
import rs.banka4.user_service.dto.AccountDto;
import rs.banka4.user_service.dto.ClientDto;
import rs.banka4.user_service.models.Client;
import rs.banka4.user_service.models.Employee;
import rs.banka4.user_service.models.Privilege;

import java.sql.Array;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
