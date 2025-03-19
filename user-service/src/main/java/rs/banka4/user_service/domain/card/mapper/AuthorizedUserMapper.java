package rs.banka4.user_service.domain.card.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import rs.banka4.user_service.domain.card.db.AuthorizedUser;
import rs.banka4.user_service.domain.card.dtos.AuthorizedUserDto;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthorizedUserMapper {

    AuthorizedUserMapper INSTANCE = Mappers.getMapper(AuthorizedUserMapper.class);

    @Mapping(
        target = "id",
        source = "userId"
    )
    AuthorizedUserDto toDto(AuthorizedUser authorizedUser);
}
