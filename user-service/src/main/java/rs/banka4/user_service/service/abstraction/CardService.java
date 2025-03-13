package rs.banka4.user_service.service.abstraction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.card.db.Card;
import rs.banka4.user_service.domain.card.dtos.CardDto;
import rs.banka4.user_service.domain.card.dtos.CreateCardDto;

public interface CardService {
    void createAuthorizedCard(Authentication auth, CreateCardDto createCardDto);
    Card blockCard(String cardNumber, String token);
    Card unblockCard(String cardNumber, String token);
    Card deactivateCard(String cardNumber, String token);
    ResponseEntity<Page<CardDto>> clientSearchCards(String accountNumber, Pageable pageable);
    ResponseEntity<Page<CardDto>> employeeSearchCards(String cardNumber, String firstName, String lastName,
                                                    String email, String cardStatus, Pageable pageable);
    void createEmployeeCard(CreateCardDto dto, Account account);
}
