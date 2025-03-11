package rs.banka4.user_service.unit.card;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.domain.card.dtos.CardDto;
import rs.banka4.user_service.domain.card.db.Card;
import rs.banka4.user_service.generator.CardObjectMother;
import rs.banka4.user_service.repositories.CardRepository;
import rs.banka4.user_service.service.impl.CardServiceImpl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class CardServiceClientSearchCardTests {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardServiceImpl cardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private static Stream<Arguments> provideAccountNumbers() {
        return Stream.of(
                Arguments.of("ACC123", true),
                Arguments.of("INVALID_ACC", false),
                Arguments.of("", false),
                Arguments.of(null, false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideAccountNumbers")
    void testClientSearchCards(String accountNumber, boolean hasCards) {
        // Arrange
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Card> cardList = hasCards ? Collections.singletonList(CardObjectMother.generateCardWithAllAttributes()) : Collections.emptyList();

        when(cardRepository.findByAccount_AccountNumber(eq(accountNumber))).thenReturn(cardList);

        // Act
        ResponseEntity<Page<CardDto>> response = cardService.clientSearchCards(accountNumber, pageRequest);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        Page<CardDto> cardPage = response.getBody();
        assertNotNull(cardPage);
        assertEquals(cardList.size(), cardPage.getTotalElements());
    }
}