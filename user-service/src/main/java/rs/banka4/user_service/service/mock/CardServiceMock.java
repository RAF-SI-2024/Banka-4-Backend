package rs.banka4.user_service.service.mock;

import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.domain.card.db.Card;
import rs.banka4.user_service.domain.card.dtos.CardDto;
import rs.banka4.user_service.domain.card.dtos.CreateAuthorizedUserDto;
import rs.banka4.user_service.service.abstraction.CardService;
import rs.banka4.user_service.service.mock.generators.CardObjectMother;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Primary
public class CardServiceMock implements CardService {
    private PageRequest pageRequest;

    @Override
    public Card createAuthorizedCard(CreateAuthorizedUserDto createAuthorizedCardDto) {
        Card card = new Card();
        card.setId(UUID.randomUUID());
        return card;
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
        List<CardDto> dtos = new ArrayList<>();
        dtos.add(CardObjectMother.generateCardDto());
        dtos.add(CardObjectMother.generateCardDto());
        dtos.add(CardObjectMother.generateCardDto());
        Page<CardDto> page = new PageImpl<>(dtos, pageRequest, 3);
        return ResponseEntity.ok(page);
    }

    @Override
    public ResponseEntity<Page<CardDto>> employeeSearchCards(String cardNumber, String firstName, String lastName, String email, String cardStatus, Pageable pageable) {
        List<CardDto> dtos = new ArrayList<>();
        dtos.add(CardObjectMother.generateCardDto());
        dtos.add(CardObjectMother.generateCardDto());
        dtos.add(CardObjectMother.generateCardDto());
        Page<CardDto> page = new PageImpl<>(dtos, pageRequest, 3);
        return ResponseEntity.ok(page);
    }
}
