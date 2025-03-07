package rs.banka4.user_service.domain.user.client.mapper;

import org.mapstruct.*;
import rs.banka4.user_service.domain.user.client.dtos.ClientDto;
import rs.banka4.user_service.domain.user.client.dtos.CreateClientDto;
import rs.banka4.user_service.domain.user.client.db.Client;

import java.util.Set;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        mappingInheritanceStrategy = MappingInheritanceStrategy.AUTO_INHERIT_ALL_FROM_CONFIG,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ClientMapper {
    Client toEntity(CreateClientDto dto);
    ClientDto toDto(Client client);

    CreateClientDto toCreateDto(ClientDto dto);

    @AfterMapping
    default void mapPrivileges(CreateClientDto dto, @MappingTarget Client client) {
//        client.setSavedContacts(Set.of());
        client.setAccounts(Set.of());
        if (dto.privilege() != null) {
            client.setPrivileges(dto.privilege());
        }
    }
}
