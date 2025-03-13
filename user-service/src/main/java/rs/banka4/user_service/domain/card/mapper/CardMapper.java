package rs.banka4.user_service.domain.card.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import rs.banka4.user_service.domain.card.db.Card;
import rs.banka4.user_service.domain.card.dtos.CardDto;
import rs.banka4.user_service.domain.card.dtos.CreateCardDto;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AuthorizedUserMapper.class})
public interface CardMapper {

    CardMapper INSTANCE = Mappers.getMapper(CardMapper.class);

    @Mapping(target = "accountNumber", source = "account.accountNumber")
    @Mapping(target = "client", source = "account.client")
    @Mapping(target = "authorizedUserDto", source = "authorizedUser")
    CardDto toDtoWithDetails(Card card);

    CardDto toDto(Card card);
    Card fromCreate(CreateCardDto cardDto);

}
