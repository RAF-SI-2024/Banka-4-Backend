package rs.banka4.user_service.domain.card.mapper;

import java.util.UUID;
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

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CardMapper {

    CardMapper INSTANCE = Mappers.getMapper(CardMapper.class);

    @Mapping(
        target = "authorizedUser",
        source = "authorizedUser",
        qualifiedByName = "mapAuthorizedUser"
    )
    Card fromCreate(CreateCardDto cardDto);

    @Named("mapCardName")
    default CardName mapCardName(Account account) {
        return account.getAccountType() == AccountType.DOO
            ? CardName.AMERICAN_EXPRESS
            : CardName.VISA;
    }

    @Mapping(
        target = "accountNumber",
        source = "account.accountNumber"
    )
    @Mapping(
        target = "client",
        source = "account.client"
    )
    @Mapping(
            target = "creationDate",
            source = "createdAt"
    )
    @Mapping(
            target = "expirationDate",
            source = "expiresAt"
    )
    @Mapping(
            target = "authorizedUserDto",
            source = "authorizedUser"
    )
    @Mapping(
            target = "authorizedUserDto.id",
            source = "authorizedUser.userId"
    )
    CardDto toDto(Card card);

    @Mapping(
            target = "accountNumber",
            source = "account.accountNumber"
    )
    @Mapping(
            target = "client",
            source = "account.client"
    )
    @Mapping(
            target = "authorizedUserDto",
            source = "authorizedUser"
    )
    @Mapping(
            target = "creationDate",
            source = "createdAt"
    )
    @Mapping(
            target = "expirationDate",
            source = "expiresAt"
    )
    @Mapping(
            target = "authorizedUserDto.id",
            source = "authorizedUser.userId"
    )
    CardDto toDtoWithDetails(Card card);

    @Named("mapAuthorizedUser")
    default AuthorizedUser map(CreateAuthorizedUserDto dto) {
        System.out.println("DTO: " + dto);
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

    @Mapping(
        target = "phoneNumber",
        source = "phone"
    )
    CreateAuthorizedUserDto toAuthorizedUserDto(AccountClientIdDto dto);

}
