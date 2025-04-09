package rs.banka4.user_service.service.mock.generators;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import rs.banka4.rafeisen.common.dto.Gender;
import rs.banka4.user_service.domain.card.db.CardName;
import rs.banka4.user_service.domain.card.db.CardStatus;
import rs.banka4.user_service.domain.card.db.CardType;
import rs.banka4.user_service.domain.card.dtos.AuthorizedUserDto;
import rs.banka4.user_service.domain.card.dtos.CardDto;

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
            "0611234567",
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
}
