package rs.banka4.user_service.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import rs.banka4.testlib.integration.DbEnabledTest;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.card.db.Card;
import rs.banka4.user_service.domain.card.db.CardName;
import rs.banka4.user_service.domain.card.db.CardStatus;
import rs.banka4.user_service.domain.card.db.CardType;
import rs.banka4.user_service.domain.currency.db.Currency;
import rs.banka4.user_service.integration.generator.UserGenerator;
import rs.banka4.user_service.repositories.AccountRepository;
import rs.banka4.user_service.repositories.CardRepository;
import rs.banka4.user_service.repositories.CurrencyRepository;
import rs.banka4.user_service.utils.JwtUtil;

@SpringBootTest
@AutoConfigureMockMvc
@DbEnabledTest
public class UnBlockCardTest {

    @Autowired
    private MockMvcTester m;

    @Autowired
    private UserGenerator userGen;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objMapper;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private AccountRepository accountRepository;

    private Account account;
    private Card card;
    private String accessToken;

    @BeforeEach
    void setUp() {
        Currency currency = Currency.builder()
                .name("Euro")
                .description("Euro currency")
                .symbol("€")
                .code(Currency.Code.EUR)
                .active(true)
                .build();
        currency = currencyRepository.saveAndFlush(currency);

        account = accountRepository.saveAndFlush(
                Account.builder()
                        .accountNumber("123456789")
                        .balance(BigDecimal.valueOf(10000))
                        .availableBalance(BigDecimal.valueOf(8000))
                        .active(true)
                        .currency(currency)
                        .build()
        );

        userGen.createEmployee(x -> x);
        var toks = userGen.doEmployeeLogin("john.doe@example.com", "test");
        accessToken = toks.accessToken();

        card = cardRepository.saveAndFlush(
                Card.builder()
                        .cardNumber("1234567810345678")
                        .cvv("123")
                        .cardName(CardName.VISA)
                        .cardType(CardType.DEBIT)
                        .account(account)
                        .cardStatus(CardStatus.ACTIVATED)
                        .limit(BigDecimal.valueOf(10000))
                        .createdAt(LocalDate.now())
                        .expiresAt(
                                LocalDate.now().plusYears(5)
                        )
                        .build()
        );
    }

    @AfterEach
    void tearDown() {
        cardRepository.deleteAll();
        accountRepository.delete(account);
        currencyRepository.deleteAll();
    }

    @Test
    void unblockCardSuccessfully() throws Exception {
        m.put()
                .uri("/cards/unblock/" + card.getCardNumber())
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatus(HttpStatus.OK);

        Card unblockedCard = cardRepository.findById(card.getId()).orElse(null);
        assertThat(unblockedCard).isNotNull();
        assertThat(unblockedCard.getCardStatus()).isEqualTo(CardStatus.ACTIVATED);
    }
}
