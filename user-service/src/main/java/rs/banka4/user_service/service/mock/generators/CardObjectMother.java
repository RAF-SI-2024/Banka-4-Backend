package rs.banka4.user_service.service.mock.generators;

import rs.banka4.user_service.domain.card.db.CardName;
import rs.banka4.user_service.domain.card.db.CardStatus;
import rs.banka4.user_service.domain.card.db.CardType;
import rs.banka4.user_service.domain.card.dtos.AuthorizedUserDto;
import rs.banka4.user_service.domain.card.dtos.CardDto;
import rs.banka4.user_service.domain.user.Gender;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class CardObjectMother {

    public static AuthorizedUserDto generateAuthorizedUserDto() {
        return new AuthorizedUserDto(
                UUID.randomUUID(),
                "Djovak",
                "Nokovic",
                LocalDate.now().minusYears(20),
                Gender.FEMALE,
                "kruska@gmail.com",
                "+38153513591",
                "Groove St 5",
                LocalDate.now(),
                LocalDate.now().plusDays(7)
        );
    }

    public static CardDto generateCardDto() {
        return new CardDto(
                UUID.randomUUID(),
                "1234567890123456",
                "331",
                CardName.VISA,
                CardType.DEBIT,
                BigDecimal.valueOf(500.00),
                CardStatus.ACTIVATED,
                generateAuthorizedUserDto()
        );
    }

}
