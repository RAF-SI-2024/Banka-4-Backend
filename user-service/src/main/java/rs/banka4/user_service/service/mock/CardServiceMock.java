package rs.banka4.user_service.service.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.card.db.Card;
import rs.banka4.user_service.domain.card.dtos.CardDto;
import rs.banka4.user_service.domain.card.dtos.CreateCardDto;
import rs.banka4.user_service.service.abstraction.CardService;
import rs.banka4.user_service.service.mock.generators.CardObjectMother;

@Service
public class CardServiceMock implements CardService {

    @Override
    public void createAuthorizedCard(Authentication auth, CreateCardDto createCardDto) {
        Card card = new Card();
        card.setId(UUID.randomUUID());
    }

    @Override
    public Card blockCard(String cardNumber, String token) {
        return null;
    }

    @Override
    public Card unblockCard(String cardNumber, String token) {
        return null;
    }

    @Override
    public Card deactivateCard(String cardNumber, String token) {
        return null;
    }

    @Override
    public ResponseEntity<Page<CardDto>> clientSearchCards(
        String token,
        String accountNumber,
        Pageable pageable
    ) {
        List<CardDto> dtos = new ArrayList<>();
        dtos.add(CardObjectMother.generateCardDto());
        dtos.add(CardObjectMother.generateCardDto());
        dtos.add(CardObjectMother.generateCardDto());
        Page<CardDto> page = new PageImpl<>(dtos, pageable, 3);
        return ResponseEntity.ok(page);
    }

    @Override
    public ResponseEntity<Page<CardDto>> employeeSearchCards(
        String token,
        String cardNumber,
        String firstName,
        String lastName,
        String email,
        String cardStatus,
        Pageable pageable
    ) {
        List<CardDto> dtos = new ArrayList<>();
        dtos.add(CardObjectMother.generateCardDto());
        dtos.add(CardObjectMother.generateCardDto());
        dtos.add(CardObjectMother.generateCardDto());
        Page<CardDto> page = new PageImpl<>(dtos, pageable, 3);
        return ResponseEntity.ok(page);
    }

    @Override
    public void createEmployeeCard(CreateCardDto dto, Account account) {
    }
}
