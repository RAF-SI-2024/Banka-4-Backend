package rs.banka4.user_service.mapper;

import org.mapstruct.*;
import rs.banka4.user_service.dto.ClientDto;
import rs.banka4.user_service.dto.requests.CreateClientDto;
import rs.banka4.user_service.dto.requests.UpdateEmployeeDto;
import rs.banka4.user_service.exceptions.PrivilegeDoesNotExist;
import rs.banka4.user_service.models.Client;
import rs.banka4.user_service.models.Employee;
import rs.banka4.user_service.models.Privilege;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        mappingInheritanceStrategy = MappingInheritanceStrategy.AUTO_INHERIT_ALL_FROM_CONFIG,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ClientMapper {
    Client toEntity(CreateClientDto dto);
    @Mapping(target = "accounts",ignore = true)
    ClientDto toDto(Client client);
    @AfterMapping
    default void mapPrivileges(CreateClientDto dto, @MappingTarget Client client) {
        client.setContacts(Set.of());
        client.setAccounts(Set.of());
        if (dto.privilege() != null) {
            client.setPrivileges(dto.privilege());
        }
    }
}
