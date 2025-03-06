package rs.banka4.user_service.mapper;

import org.mapstruct.*;
import rs.banka4.user_service.dto.ClientDto;
import rs.banka4.user_service.models.Client;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import rs.banka4.user_service.dto.requests.CreateClientDto;
import java.util.Set;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        mappingInheritanceStrategy = MappingInheritanceStrategy.AUTO_INHERIT_ALL_FROM_CONFIG,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ClientMapper {
    Client toEntity(CreateClientDto dto);
    @Mapping(target = "accounts",ignore = true)
    ClientDto toDto(Client client);

    CreateClientDto toCreateDto(ClientDto dto);

    @AfterMapping
    default void mapPrivileges(CreateClientDto dto, @MappingTarget Client client) {
        client.setContacts(Set.of());
        client.setAccounts(Set.of());
        if (dto.privilege() != null) {
            client.setPrivileges(dto.privilege());
        }
    }
}

