package rs.banka4.user_service.domain.user.client.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import rs.banka4.user_service.domain.user.User;
import rs.banka4.user_service.domain.user.client.dtos.ClientDto;
import rs.banka4.user_service.domain.user.client.dtos.CreateClientDto;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.domain.user.client.dtos.UpdateClientDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClientMapper {

    ClientMapper INSTANCE = Mappers.getMapper(ClientMapper.class);

    @Mapping(target = "gender", source = "gender", qualifiedByName = "mapGender")
    Client toEntity(CreateClientDto dto);
    ClientDto toDto(Client client);

    @Mapping(target = "gender", source = "gender", qualifiedByName = "mapGender")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void fromUpdate(@MappingTarget Client target, UpdateClientDto dto);

    @Named("mapGender")
    default User.Gender mapGender(String gender) {
        return gender != null ? User.Gender.valueOf(gender.toUpperCase()) : null;
    }

    @AfterMapping
    default void mapPrivileges(CreateClientDto dto, @MappingTarget Client client) {
        if (dto.privilege() != null) {
            client.setPrivileges(dto.privilege());
        }
    }
}
