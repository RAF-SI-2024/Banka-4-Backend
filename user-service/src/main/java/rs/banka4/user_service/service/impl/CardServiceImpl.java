package rs.banka4.user_service.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.domain.card.db.Card;
import rs.banka4.user_service.domain.card.dtos.CardDto;
import rs.banka4.user_service.domain.card.dtos.CreateCardDto;
import rs.banka4.user_service.service.abstraction.CardService;

public class CardServiceImpl implements CardService {
    @Override
    public Card createAuthorizedCard(CreateCardDto createCardDto) {
        return null;
    }

    @Override
    public Card blockCard(String cardNumber) {
        return null;
    }

    @Override
    public Card unblockCard(String cardNumber) {
        return null;
    }

    @Override
    public Card deactivateCard(String cardNumber) {
        return null;
    }

    @Override
    public ResponseEntity<Page<CardDto>> clientSearchCards(String accountNumber, Pageable pageable) {
        return null;
        // check out /client/search
    }

    @Override
    public ResponseEntity<Page<CardDto>> employeeSearchCards(String cardNumber, String firstName, String lastName, String email, String cardStatus, Pageable pageable) {
        return null;
        // check out /client/search
    }
}
