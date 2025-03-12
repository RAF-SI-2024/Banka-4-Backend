package rs.banka4.user_service.generator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import rs.banka4.user_service.domain.card.db.CardName;
import rs.banka4.user_service.domain.card.db.CardStatus;
import rs.banka4.user_service.domain.card.db.CardType;
import rs.banka4.user_service.domain.card.dtos.AuthorizedUserDto;
import rs.banka4.user_service.domain.card.dtos.CardDto;
import rs.banka4.user_service.domain.card.dtos.CreateAuthorizedUserDto;
import rs.banka4.user_service.domain.card.dtos.CreateCardDto;
import rs.banka4.user_service.domain.user.Gender;
import rs.banka4.user_service.service.mock.generators.ClientObjectMother;

public class CardObjectMother {

    public static AuthorizedUserDto generateAuthorizedUserDto() {
        return new AuthorizedUserDto(
            UUID.randomUUID(),
            "Djovak",
            "Nokovic",
            LocalDate.now()
                .minusYears(20),
            Gender.FEMALE,
            "kruska@gmail.com",
            "+38153513591",
            "Groove St 5"
        );
    }

    public static CardDto generateCardDto() {
        return new CardDto(
            "1234567890123456",
            "331",
            CardName.VISA,
            LocalDate.now()
                .minusYears(10),
            LocalDate.now()
                .minusYears(5),
            CardType.DEBIT,
            BigDecimal.valueOf(500.00),
            CardStatus.ACTIVATED,
            "215351385938112",
            ClientObjectMother.generateBasicClientDto(),
            generateAuthorizedUserDto()
        );
    }

    public static CreateCardDto validRequest() {
        return new CreateCardDto(
            "44400011234567890",
            new CreateAuthorizedUserDto(
                "Mehmedalija",
                "Krupalija",
                LocalDate.parse("1995-05-05"),
                Gender.MALE,
                "mehmedalija@banka4.rs",
                "+38766111222",
                "Bankarska 4"
            ),
            "123456"
        );
    }

}
