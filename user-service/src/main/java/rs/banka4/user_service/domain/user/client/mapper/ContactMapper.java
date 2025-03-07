package rs.banka4.user_service.domain.user.client.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import rs.banka4.user_service.domain.user.client.dtos.ClientContactDto;
import rs.banka4.user_service.domain.account.db.Account;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ContactMapper {

    @Mapping(source = "client.firstName", target = "firstName")
    @Mapping(source = "client.lastName", target = "lastName")
    ClientContactDto toClientContactDto(Account account);

}
