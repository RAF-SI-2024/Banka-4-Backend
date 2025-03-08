package rs.banka4.user_service.domain.user.client.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import rs.banka4.user_service.domain.user.client.dtos.ClientDto;
import rs.banka4.user_service.domain.user.client.dtos.CreateClientDto;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.domain.user.client.dtos.UpdateClientDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClientMapper {

    ClientMapper INSTANCE = Mappers.getMapper(ClientMapper.class);

    Client toEntity(CreateClientDto dto);
    ClientDto toDto(Client client);

    void fromUpdate(@MappingTarget Client target, UpdateClientDto dto);

    @AfterMapping
    default void mapPrivileges(CreateClientDto dto, @MappingTarget Client client) {
        if (dto.privilege() != null) {
            client.setPrivileges(dto.privilege());
        }
    }
}
