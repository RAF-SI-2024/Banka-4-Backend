package rs.banka4.user_service.domain.user.client.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import rs.banka4.user_service.domain.user.client.db.ClientContact;
import rs.banka4.user_service.domain.user.client.dtos.ClientContactDto;
import rs.banka4.user_service.domain.user.client.dtos.ClientContactRequest;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClientContactMapper {

    ClientContactMapper INSTANCE = Mappers.getMapper(ClientContactMapper.class);

    ClientContactDto toDto(ClientContact clientContact);
    ClientContact toEntity(ClientContactDto clientContactDto);
    ClientContact toEntity(ClientContactRequest clientContactDto);

    void fromUpdate(@MappingTarget ClientContact clientContact, ClientContactRequest request);

}
