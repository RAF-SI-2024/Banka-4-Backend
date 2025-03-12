package rs.banka4.user_service.domain.card.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import rs.banka4.user_service.domain.card.db.AuthorizedUser;
import rs.banka4.user_service.domain.card.db.Card;
import rs.banka4.user_service.domain.card.dtos.CardDto;
import rs.banka4.user_service.domain.card.dtos.CreateAuthorizedUserDto;
import rs.banka4.user_service.domain.card.dtos.CreateCardDto;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CardMapper {

    @Mapping(target = "authorizedUser", source = "createAuthorizedUserDto", qualifiedByName = "mapAuthorizedUser")
    @Mapping(target = "cardType", constant = "DEBIT")
    @Mapping(target = "cardStatus", constant = "ACTIVATED")
    Card fromCreate(CreateCardDto cardDto);

    @Mapping(target = "accountNumber", source = "account.accountNumber")
    @Mapping(target = "client", source = "account.client")
    CardDto toDto(Card card);

    @Named("mapAuthorizedUser")
    default AuthorizedUser map(CreateAuthorizedUserDto dto) {
        if (dto == null) return null;
        return new AuthorizedUser(
                UUID.randomUUID(),
                dto.firstName(),
                dto.lastName(),
                dto.dateOfBirth(),
                dto.email(),
                dto.phoneNumber(),
                dto.address(),
                dto.gender()
        );
    }
}