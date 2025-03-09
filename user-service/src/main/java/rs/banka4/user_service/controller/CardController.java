package rs.banka4.user_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.banka4.user_service.controller.docs.CardDocumentation;
import rs.banka4.user_service.domain.card.dtos.CreateAuthorizedCardDto;
import rs.banka4.user_service.service.abstraction.CardService;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
public class CardController implements CardDocumentation {
    CardService cardService;

    @Override
    @GetMapping("/create")
    public ResponseEntity<Void> createAuthorizedCard(CreateAuthorizedCardDto createAuthorizedCardDto) {
        cardService.createAuthorizedCard(createAuthorizedCardDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
