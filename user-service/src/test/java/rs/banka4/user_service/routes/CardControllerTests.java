package rs.banka4.user_service.routes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import rs.banka4.user_service.controller.CardController;
import rs.banka4.user_service.domain.card.db.Card;
import rs.banka4.user_service.domain.card.db.CardStatus;
import rs.banka4.user_service.service.abstraction.CardService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CardControllerTests {

    @Mock
    private CardService cardService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CardController cardController;

    private static final String TEST_CARD_NUMBER = "1234567810345678";
    private static final String EMPLOYEE_TOKEN = "employeeToken";
    private static final String CLIENT_TOKEN = "clientToken";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void blockCard_ClientOwnsCard_ShouldBlockSuccessfully() {
        Card card = new Card();
        card.setCardStatus(CardStatus.ACTIVATED);

        when(authentication.getCredentials()).thenReturn(CLIENT_TOKEN);
        when(cardService.blockCard(eq(TEST_CARD_NUMBER), eq(CLIENT_TOKEN))).thenReturn(card);

        ResponseEntity<Void> response = cardController.blockCard(authentication, TEST_CARD_NUMBER);

        assertEquals(200, response.getStatusCode().value());
        verify(cardService, times(1)).blockCard(eq(TEST_CARD_NUMBER), eq(CLIENT_TOKEN));
    }

    @Test
    void blockCard_ClientDoesNotOwnCard_ShouldReturnNotFound() {
        when(authentication.getCredentials()).thenReturn(CLIENT_TOKEN);
        when(cardService.blockCard(eq(TEST_CARD_NUMBER), eq(CLIENT_TOKEN))).thenReturn(null);

        ResponseEntity<Void> response = cardController.blockCard(authentication, TEST_CARD_NUMBER);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void unblockCard_EmployeeUnblocksSuccessfully_ShouldReturnOk() {
        Card card = new Card();
        card.setCardStatus(CardStatus.BLOCKED);

        when(authentication.getCredentials()).thenReturn(EMPLOYEE_TOKEN);
        when(cardService.unblockCard(eq(TEST_CARD_NUMBER), eq(EMPLOYEE_TOKEN))).thenReturn(card);

        ResponseEntity<Void> response = cardController.unblockCard(authentication, TEST_CARD_NUMBER);

        assertEquals(200, response.getStatusCode().value());
        verify(cardService, times(1)).unblockCard(eq(TEST_CARD_NUMBER), eq(EMPLOYEE_TOKEN));
    }

    @Test
    void unblockCard_ClientTriesToUnblock_ShouldReturnBadRequest() {
        when(authentication.getCredentials()).thenReturn(CLIENT_TOKEN);
        when(cardService.unblockCard(eq(TEST_CARD_NUMBER), eq(CLIENT_TOKEN))).thenReturn(null);

        ResponseEntity<Void> response = cardController.unblockCard(authentication, TEST_CARD_NUMBER);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void unblockCard_IfDeactivated_ShouldReturnBadRequest() {
        Card card = new Card();
        card.setCardStatus(CardStatus.DEACTIVATED);

        when(authentication.getCredentials()).thenReturn(EMPLOYEE_TOKEN);
        when(cardService.unblockCard(eq(TEST_CARD_NUMBER), eq(EMPLOYEE_TOKEN))).thenReturn(card);

        ResponseEntity<Void> response = cardController.unblockCard(authentication, TEST_CARD_NUMBER);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void deactivateCard_EmployeeDeactivatesSuccessfully_ShouldReturnOk() {
        Card card = new Card();
        card.setCardStatus(CardStatus.ACTIVATED);

        when(authentication.getCredentials()).thenReturn(EMPLOYEE_TOKEN);
        when(cardService.deactivateCard(eq(TEST_CARD_NUMBER), eq(EMPLOYEE_TOKEN))).thenReturn(card);

        ResponseEntity<Void> response = cardController.deactivateCard(authentication, TEST_CARD_NUMBER);

        assertEquals(200, response.getStatusCode().value());
        verify(cardService, times(1)).deactivateCard(eq(TEST_CARD_NUMBER), eq(EMPLOYEE_TOKEN));
    }

    @Test
    void deactivateCard_AlreadyDeactivated_ShouldReturnBadRequest() {
        when(authentication.getCredentials()).thenReturn(EMPLOYEE_TOKEN);
        when(cardService.deactivateCard(eq(TEST_CARD_NUMBER), eq(EMPLOYEE_TOKEN))).thenReturn(null);

        ResponseEntity<Void> response = cardController.deactivateCard(authentication, TEST_CARD_NUMBER);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void deactivateCard_ClientTriesToDeactivate_ShouldReturnBadRequest() {
        when(authentication.getCredentials()).thenReturn(CLIENT_TOKEN);
        when(cardService.deactivateCard(eq(TEST_CARD_NUMBER), eq(CLIENT_TOKEN))).thenReturn(null);

        ResponseEntity<Void> response = cardController.deactivateCard(authentication, TEST_CARD_NUMBER);

        assertEquals(400, response.getStatusCode().value());
    }
}
