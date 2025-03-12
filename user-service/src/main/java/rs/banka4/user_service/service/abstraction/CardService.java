package rs.banka4.user_service.service.abstraction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.domain.card.db.Card;
import rs.banka4.user_service.domain.card.dtos.CardDto;
import rs.banka4.user_service.domain.card.dtos.CreateCardDto;

public interface CardService {
    Card createAuthorizedCard(CreateCardDto createCardDto);
    Card blockCard(String cardNumber);
    Card unblockCard(String cardNumber);
    Card deactivateCard(String cardNumber);
    ResponseEntity<Page<CardDto>> clientSearchCards(String token, String accountNumber, Pageable pageable);
    ResponseEntity<Page<CardDto>> employeeSearchCards(String token, String cardNumber, String firstName, String lastName,
                                                    String email, String cardStatus, Pageable pageable);
}
