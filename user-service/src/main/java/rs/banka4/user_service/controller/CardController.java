package rs.banka4.user_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.banka4.user_service.controller.docs.CardDocumentation;
import rs.banka4.user_service.domain.card.db.Card;
import rs.banka4.user_service.domain.card.dtos.CardDto;
import rs.banka4.user_service.domain.card.dtos.CreateAuthorizedUserDto;
import rs.banka4.user_service.service.abstraction.CardService;

import java.util.UUID;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
public class CardController implements CardDocumentation {
    CardService cardService;

    @Override
    @PostMapping("/create")
    public ResponseEntity<UUID> createAuthorizedCard(@RequestBody @Valid CreateAuthorizedUserDto createAuthorizedUserDto) {
        Card card = cardService.createAuthorizedCard(createAuthorizedUserDto);
        return ResponseEntity.ok(card.getId());
    }

    @Override
    @PutMapping("/block/{cardNumber}")
    public ResponseEntity<Void> blockCard(@PathVariable("cardNumber") String cardNumber) {
        cardService.blockCard(cardNumber);
        return ResponseEntity.ok().build();
    }

    @Override
    @PutMapping("/unblock/{cardNumber}")
    public ResponseEntity<Void> unblockCard(@PathVariable("cardNumber") String cardNumber) {
        cardService.unblockCard(cardNumber);
        return ResponseEntity.ok().build();
    }

    @Override
    @PutMapping("/unblock/{cardNumber}")
    public ResponseEntity<Void> deactivateCard(@PathVariable("cardNumber") String cardNumber) {
        cardService.deactivateCard(cardNumber);
        return ResponseEntity.ok().build();
    }

    @Override
    @GetMapping("client/search")
    public ResponseEntity<Page<CardDto>> clientSearchCards(
            @RequestParam(required = false) String accountNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return cardService.clientSearchCards(accountNumber, PageRequest.of(page, size));
    }

    @Override
    @GetMapping("employee/search")
    public ResponseEntity<Page<CardDto>> employeeSearchCards(@RequestParam(required = false) String cardNumer,
                                                             @RequestParam(required = false) String firstName,
                                                             @RequestParam(required = false) String lastName,
                                                             @RequestParam(required = false) String email,
                                                             @RequestParam(required = false) String cardStatus,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "10") int size) {
        return cardService.employeeSearchCards(cardNumer, firstName, lastName, email, cardStatus, PageRequest.of(page, size));
    }
}