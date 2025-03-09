package rs.banka4.user_service.service.abstraction;

import rs.banka4.user_service.domain.card.db.Card;
import rs.banka4.user_service.domain.card.dtos.CreateAuthorizedCardDto;

public interface CardService {
    Card createAuthorizedCard(CreateAuthorizedCardDto createAuthorizedCardDto);
}
