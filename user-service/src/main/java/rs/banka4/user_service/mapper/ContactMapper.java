package rs.banka4.user_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import rs.banka4.user_service.dto.ClientContactDto;
import rs.banka4.user_service.models.Account;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ContactMapper {

    @Mapping(source = "client.firstName", target = "firstName")
    @Mapping(source = "client.lastName", target = "lastName")
    ClientContactDto toClientContactDto(Account account);

}
