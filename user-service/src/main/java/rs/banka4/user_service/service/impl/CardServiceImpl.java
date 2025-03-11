package rs.banka4.user_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.domain.card.db.Card;
import rs.banka4.user_service.domain.card.db.CardStatus;
import rs.banka4.user_service.domain.card.dtos.CardDto;
import rs.banka4.user_service.domain.card.dtos.CreateCardDto;
import rs.banka4.user_service.repositories.CardRepository;
import rs.banka4.user_service.service.abstraction.CardService;

import java.util.Optional;
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;

    @Override
    public Card createAuthorizedCard(CreateCardDto createCardDto) {
        return null;
    }

    @Override
    public Card blockCard(String cardNumber) {

        Optional<Card> optionalCard = cardRepository.findCardByCardNumber(cardNumber);

        if (optionalCard.isEmpty()) {
            return null;
        }
        Card card = optionalCard.get();

        if (card.getCardStatus().equals(CardStatus.BLOCKED) || card.getCardStatus() == CardStatus.DEACTIVATED) {
            return card;
        }

        card.setCardStatus(CardStatus.BLOCKED);
        return cardRepository.save(card);
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
