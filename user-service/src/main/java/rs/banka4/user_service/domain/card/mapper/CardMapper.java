package rs.banka4.user_service.domain.card.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.account.db.AccountType;
import rs.banka4.user_service.domain.account.dtos.AccountClientIdDto;
import rs.banka4.user_service.domain.card.db.AuthorizedUser;
import rs.banka4.user_service.domain.card.db.Card;
import rs.banka4.user_service.domain.card.db.CardName;
import rs.banka4.user_service.domain.card.dtos.CardDto;
import rs.banka4.user_service.domain.card.dtos.CreateAuthorizedUserDto;
import rs.banka4.user_service.domain.card.dtos.CreateCardDto;
import rs.banka4.user_service.domain.user.Gender;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CardMapper {

    CardMapper INSTANCE = Mappers.getMapper(CardMapper.class);

    @Mapping(target = "authorizedUser", source = "authorizedUser", qualifiedByName = "mapAuthorizedUser")
    Card fromCreate(CreateCardDto cardDto);

    @Named("mapCardName")
    default CardName mapCardName(Account account) {
        return account.getAccountType() == AccountType.DOO
                ? CardName.AMERICAN_EXPRESS
                : CardName.VISA;
    }

    @Mapping(target = "accountNumber", source = "account.accountNumber")
    @Mapping(target = "client", source = "account.client")
    CardDto toDto(Card card);

    @Named("mapAuthorizedUser")
    @Mapping(target = "gender", source = "gender", qualifiedByName = "mapGender")
    AuthorizedUser map(CreateAuthorizedUserDto dto);

    @Mapping(target = "phoneNumber", source = "phone")
    CreateAuthorizedUserDto toAuthorizedUserDto(AccountClientIdDto dto);

    @Named("mapGender")
    default Gender mapGender(String gender) {
        return gender != null ? Gender.valueOf(gender.toUpperCase()) : null;
    }

}