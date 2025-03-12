package rs.banka4.user_service.domain.user.client.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import rs.banka4.user_service.domain.account.dtos.AccountClientIdDto;
import rs.banka4.user_service.domain.user.client.dtos.ClientDto;
import rs.banka4.user_service.domain.user.client.dtos.CreateClientDto;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.domain.user.client.dtos.UpdateClientDto;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClientMapper {

    ClientMapper INSTANCE = Mappers.getMapper(ClientMapper.class);

    Client toEntity(CreateClientDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "permissionBits", ignore = true)
    Client toEntity(AccountClientIdDto dto);

    ClientDto toDto(Client client);

    @Mapping(target = "phone", source = "phoneNumber")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void fromUpdate(@MappingTarget Client target, UpdateClientDto dto);


    @AfterMapping
    default void mapPrivileges(CreateClientDto dto, @MappingTarget Client client) {
        if (dto.privilege() != null) {
            client.setPrivileges(dto.privilege());
        }
    }

    @Mapping(target = "has2FA", source = "has2FA")
    ClientDto toDto(Client client, Boolean has2FA);

}
