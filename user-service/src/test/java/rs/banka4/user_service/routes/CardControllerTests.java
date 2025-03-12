package rs.banka4.user_service.routes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.controller.CardController;
import rs.banka4.user_service.domain.card.db.Card;
import rs.banka4.user_service.domain.card.db.CardStatus;
import rs.banka4.user_service.service.abstraction.CardService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CardControllerTests {

    @Mock
    private CardService cardService;

    @InjectMocks
    private CardController cardController;

    private static final String TEST_CARD_NUMBER = "1234567810345678";
    private static final String EMPLOYEE_TOKEN = "Bearer employeeToken";
    private static final String CLIENT_TOKEN = "Bearer clientToken";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void blockCard_ClientOwnsCard_ShouldBlockSuccessfully() {
        Card card = new Card();
        card.setCardStatus(CardStatus.ACTIVATED);
        when(cardService.blockCard(eq(TEST_CARD_NUMBER), anyString())).thenReturn(card);

        ResponseEntity<Void> response = cardController.blockCard(TEST_CARD_NUMBER, CLIENT_TOKEN);

        assertEquals(200, response.getStatusCode().value());
        verify(cardService, times(1)).blockCard(eq(TEST_CARD_NUMBER), anyString());
    }

    @Test
    void blockCard_ClientDoesNotOwnCard_ShouldReturnNotFound() {
        when(cardService.blockCard(eq(TEST_CARD_NUMBER), anyString())).thenReturn(null);

        ResponseEntity<Void> response = cardController.blockCard(TEST_CARD_NUMBER, CLIENT_TOKEN);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void unblockCard_EmployeeUnblocksSuccessfully_ShouldReturnOk() {
        when(cardService.unblockCard(eq(TEST_CARD_NUMBER), anyString())).thenReturn(new Card());

        ResponseEntity<Void> response = cardController.unblockCard(TEST_CARD_NUMBER, EMPLOYEE_TOKEN);

        assertEquals(200, response.getStatusCode().value());
        verify(cardService, times(1)).unblockCard(eq(TEST_CARD_NUMBER), anyString());
    }

    @Test
    void unblockCard_ClientTriesToUnblock_ShouldReturnForbidden() {
        when(cardService.unblockCard(eq(TEST_CARD_NUMBER), anyString())).thenReturn(null);

        ResponseEntity<Void> response = cardController.unblockCard(TEST_CARD_NUMBER, CLIENT_TOKEN);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void deactivateCard_EmployeeDeactivatesSuccessfully_ShouldReturnOk() {
        Card card = new Card();
        card.setCardStatus(CardStatus.ACTIVATED);
        when(cardService.deactivateCard(eq(TEST_CARD_NUMBER), anyString())).thenReturn(card);

        ResponseEntity<Void> response = cardController.deactivateCard(TEST_CARD_NUMBER, EMPLOYEE_TOKEN);

        assertEquals(200, response.getStatusCode().value());
        verify(cardService, times(1)).deactivateCard(eq(TEST_CARD_NUMBER), anyString());
    }

    @Test
    void deactivateCard_AlreadyDeactivated_ShouldReturnBadRequest() {
        when(cardService.deactivateCard(eq(TEST_CARD_NUMBER), anyString())).thenReturn(null);

        ResponseEntity<Void> response = cardController.deactivateCard(TEST_CARD_NUMBER, EMPLOYEE_TOKEN);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void deactivateCard_ClientTriesToDeactivate_ShouldReturnForbidden() {
        when(cardService.deactivateCard(eq(TEST_CARD_NUMBER), anyString())).thenReturn(null);

        ResponseEntity<Void> response = cardController.deactivateCard(TEST_CARD_NUMBER, CLIENT_TOKEN);

        assertEquals(400, response.getStatusCode().value());
    }
}
