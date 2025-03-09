package rs.banka4.user_service.service.mock;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.domain.card.db.Card;
import rs.banka4.user_service.domain.card.dtos.CreateAuthorizedCardDto;
import rs.banka4.user_service.service.abstraction.CardService;

@Service
@Primary
public class CardServiceMock implements CardService {
    @Override
    public Card createAuthorizedCard(CreateAuthorizedCardDto createAuthorizedCardDto) {
        return null;
    }
}
