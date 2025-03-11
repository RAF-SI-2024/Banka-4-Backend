package rs.banka4.user_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.domain.card.db.Card;
import rs.banka4.user_service.domain.card.dtos.CardDto;
import rs.banka4.user_service.domain.card.dtos.CreateCardDto;
import rs.banka4.user_service.domain.card.mapper.CardMapper;
import rs.banka4.user_service.exceptions.NullPageRequest;
import rs.banka4.user_service.repositories.CardRepository;
import rs.banka4.user_service.service.abstraction.CardService;
import rs.banka4.user_service.utils.specification.CardSpecification;
import rs.banka4.user_service.utils.specification.SpecificationCombinator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

        List<Card> clientCards = cardRepository.findByAccount_AccountNumber(accountNumber);

        List<CardDto> cardDtos = clientCards.stream().map(CardMapper.INSTANCE::toDto).toList();

        Page<CardDto> pagedClientCards = new PageImpl<>(cardDtos, pageable, clientCards.size());

        return ResponseEntity.ok(pagedClientCards);
    }

    @Override
    public ResponseEntity<Page<CardDto>> employeeSearchCards(String cardNumber, String firstName, String lastName, String email, String cardStatus, Pageable pageable) {
        if (pageable == null) {
            throw new NullPageRequest();
        }

        SpecificationCombinator<Card> combinator = new SpecificationCombinator<>();

        if (cardNumber != null && !cardNumber.isEmpty()) {
            combinator.and(CardSpecification.hasCardNumber(cardNumber));
        }
        if (firstName != null && !firstName.isEmpty()) {
            combinator.and(CardSpecification.hasFirstName(firstName));
        }
        if (lastName != null && !lastName.isEmpty()) {
            combinator.and(CardSpecification.hasLastName(lastName));
        }
        if (email != null && !email.isEmpty()) {
            combinator.and(CardSpecification.hasEmail(email));
        }
        if (cardStatus != null && !cardStatus.isEmpty()) {
            combinator.and(CardSpecification.hasCardStatus(cardStatus));
        }

        Page<Card> cards = cardRepository.findAll(combinator.build(), pageable);
        Page<CardDto> dtos = cards.map(CardMapper.INSTANCE::toDto);

        return ResponseEntity.ok(dtos);
    }
}
