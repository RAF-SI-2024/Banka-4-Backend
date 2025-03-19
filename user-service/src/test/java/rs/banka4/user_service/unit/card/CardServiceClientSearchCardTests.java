package rs.banka4.user_service.unit.card;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.card.db.Card;
import rs.banka4.user_service.domain.card.dtos.CardDto;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.generator.CardObjectMother;
import rs.banka4.user_service.repositories.AccountRepository;
import rs.banka4.user_service.repositories.CardRepository;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.service.impl.CardServiceImpl;
import rs.banka4.user_service.utils.JwtUtil;

public class CardServiceClientSearchCardTests {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardServiceImpl cardService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private AccountRepository accountRepository;

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
        List<Card> cardList =
            hasCards
                ? Collections.singletonList(CardObjectMother.generateCardWithAllAttributes())
                : Collections.emptyList();
        Page<Card> cardPage = new PageImpl<>(cardList, pageRequest, cardList.size());

        String token = "mocked-token";
        String email = "johndoe@example.com";
        Client mockClient = new Client();
        Account mockAccount = new Account();
        mockAccount.setAccountNumber(accountNumber);
        Set<Account> mockAccounts = Set.of(mockAccount);

        // Mock JWT token extraction
        when(jwtUtil.extractUsername(token)).thenReturn(email);

        // Mock repository calls
        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(mockClient));
        when(accountRepository.findAllByClient(mockClient)).thenReturn(mockAccounts);
        when(cardRepository.findByAccountAccountNumber(eq(accountNumber))).thenReturn(cardList);

        // Act
        ResponseEntity<Page<CardDto>> response =
            cardService.clientSearchCards(token, accountNumber, pageRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Page<CardDto> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(cardList.size(), responseBody.getTotalElements());
    }
}
