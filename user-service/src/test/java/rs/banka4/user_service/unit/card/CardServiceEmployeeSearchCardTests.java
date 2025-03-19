package rs.banka4.user_service.unit.card;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.domain.card.db.Card;
import rs.banka4.user_service.domain.card.dtos.CardDto;
import rs.banka4.user_service.generator.CardObjectMother;
import rs.banka4.user_service.repositories.CardRepository;
import rs.banka4.user_service.service.impl.CardServiceImpl;
import rs.banka4.user_service.utils.JwtUtil;

public class CardServiceEmployeeSearchCardTests {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardServiceImpl cardService;

    @Mock
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private static Stream<Arguments> provideFilters() {
        return Stream.of(
            Arguments.of("1234567812345678", "John", "Doe", "user@example.com", "ACTIVATED"),
            Arguments.of("1234567812345678", null, null, null, null),
            Arguments.of(null, "John", null, null, null),
            Arguments.of(null, null, "Doe", null, null),
            Arguments.of(null, null, null, "user@example.com", null),
            Arguments.of(null, null, null, null, "BLOCKED"),
            Arguments.of(null, null, null, null, null)
        );
    }

    @ParameterizedTest
    @MethodSource("provideFilters")
    void testEmployeeSearchCards(
        String cardNumber,
        String firstName,
        String lastName,
        String email,
        String cardStatus
    ) {
        // Arrange
        PageRequest pageRequest = PageRequest.of(0, 10);
        Card card = CardObjectMother.generateCardWithAllAttributes();
        Page<Card> cardPage = new PageImpl<>(Collections.singletonList(card));

        String token = "mocked-token";

        // Mock JWT role extraction
        when(jwtUtil.extractRole(token)).thenReturn("employee");

        // Mock repository call
        when(cardRepository.findAll(any(Specification.class), eq(pageRequest))).thenReturn(
            cardPage
        );

        // Act
        ResponseEntity<Page<CardDto>> response =
            cardService.employeeSearchCards(
                token,
                cardNumber,
                firstName,
                lastName,
                email,
                cardStatus,
                pageRequest
            );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(
            1,
            response.getBody()
                .getTotalElements()
        );
    }
}
