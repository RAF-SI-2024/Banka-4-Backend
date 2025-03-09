package rs.banka4.user_service.domain.card.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import rs.banka4.user_service.domain.card.db.Card;
import rs.banka4.user_service.domain.card.dtos.CardDto;
import rs.banka4.user_service.domain.card.dtos.CreateCardDto;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CardMapper {

    CardMapper INSTANCE = Mappers.getMapper(CardMapper.class);

    CardDto toDto(Card card);
    Card fromCreate(CreateCardDto cardDto);
}
