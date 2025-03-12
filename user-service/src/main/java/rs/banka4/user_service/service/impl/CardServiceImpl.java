package rs.banka4.user_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.domain.card.db.Card;
import rs.banka4.user_service.domain.card.db.CardStatus;
import rs.banka4.user_service.domain.card.dtos.CardDto;
import rs.banka4.user_service.domain.card.dtos.CreateCardDto;
import rs.banka4.user_service.repositories.CardRepository;
import rs.banka4.user_service.service.abstraction.CardService;
import rs.banka4.user_service.utils.JwtUtil;


import java.util.Optional;
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final JwtUtil jwtUtil;

    @Override
    public Card createAuthorizedCard(CreateCardDto createCardDto) {
        return null;
    }

    @Override
    public Card blockCard(String cardNumber, String token) {
        Optional<Card> optionalCard = cardRepository.findCardByCardNumber(cardNumber);
        if (optionalCard.isEmpty()) {
            return null;
        }

        Card card = optionalCard.get();
        String role = jwtUtil.extractRole(token);
        String userId = jwtUtil.extractClaim(token, claims -> claims.get("id", String.class));
        String email = jwtUtil.extractUsername(token);

        if ("client".equalsIgnoreCase(role)) {
            if (card.getAccount() == null || card.getAccount().getClient() == null) {
                return null;
            }
            String ownerId = card.getAccount().getClient().getId().toString();
            String ownerEmail = card.getAccount().getClient().getEmail();

            if (!userId.equals(ownerId) && !email.equals(ownerEmail)) {
                return null;
            }
        }
        if (card.getCardStatus() == CardStatus.BLOCKED || card.getCardStatus() == CardStatus.DEACTIVATED) {
            return card;
        }
        card.setCardStatus(CardStatus.BLOCKED);
        return cardRepository.save(card);
    }


    @Override
    public Card unblockCard(String cardNumber, String token) {
        Optional<Card> optionalCard = cardRepository.findCardByCardNumber(cardNumber);
        if (optionalCard.isEmpty()) {
            return null;
        }

        Card card = optionalCard.get();
        String role = jwtUtil.extractRole(token);

        if (!"employee".equalsIgnoreCase(role)) {
            return null;
        }

        if (card.getCardStatus() != CardStatus.BLOCKED) {
            return card;
        }

        card.setCardStatus(CardStatus.ACTIVATED);
        return cardRepository.save(card);
    }


    @Override
    public Card deactivateCard(String cardNumber, String token) {
        Optional<Card> optionalCard = cardRepository.findCardByCardNumber(cardNumber);
        if (optionalCard.isEmpty()) {
            return null;
        }

        Card card = optionalCard.get();
        String role = jwtUtil.extractRole(token);

        if (!"employee".equalsIgnoreCase(role)) {
            return null;
        }

        if (card.getCardStatus() == CardStatus.DEACTIVATED) {
            return null;
        }

        card.setCardStatus(CardStatus.DEACTIVATED);
        return cardRepository.save(card);
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
