package rs.banka4.user_service.domain.user.client.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import rs.banka4.user_service.domain.account.dtos.AccountClientIdDto;
import rs.banka4.user_service.domain.user.Gender;
import rs.banka4.user_service.domain.user.client.dtos.ClientDto;
import rs.banka4.user_service.domain.user.client.dtos.CreateClientDto;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.domain.user.client.dtos.UpdateClientDto;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClientMapper {

    ClientMapper INSTANCE = Mappers.getMapper(ClientMapper.class);

    @Mapping(target = "gender", source = "gender", qualifiedByName = "mapGender")
    Client toEntity(CreateClientDto dto);

    @Mapping(target = "gender", source = "gender", qualifiedByName = "mapGender")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "permissionBits", ignore = true)
    Client toEntity(AccountClientIdDto dto);

    ClientDto toDto(Client client);

    @Mapping(target = "gender", source = "gender", qualifiedByName = "mapGender")
    @Mapping(target = "phone", source = "phoneNumber")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void fromUpdate(@MappingTarget Client target, UpdateClientDto dto);

    @Named("mapGender")
    default Gender mapGender(String gender) {
        return gender != null ? Gender.valueOf(gender.toUpperCase()) : null;
    }

    @AfterMapping
    default void mapPrivileges(CreateClientDto dto, @MappingTarget Client client) {
        if (dto.privilege() != null) {
            client.setPrivileges(dto.privilege());
        }
    }

    @Mapping(target = "has2FA", source = "has2FA")
    ClientDto toDto(Client client, Boolean has2FA);

}
