package rs.banka4.user_service.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import rs.banka4.user_service.domain.card.db.CardStatus;
import rs.banka4.user_service.domain.currency.db.Currency;
import rs.banka4.user_service.integration.generator.UserGenerator;
import rs.banka4.user_service.integration.seeder.TestDataSeeder;
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
    @Autowired
    private TestDataSeeder testDataSeeder;

    private Account account;
    private Card blockedCard;
    private Card activeCard;
    private Card deactivatedCard;
    private String accessToken;

    @BeforeEach
    void setUp() {
        account = testDataSeeder.seedAccount();
        Currency currency = testDataSeeder.seedCurrency();

        userGen.createEmployee(x -> x);
        var toks = userGen.doEmployeeLogin("john.doe@example.com", "test");
        accessToken = toks.accessToken();

        blockedCard = testDataSeeder.seedBlockedCard(account);
        activeCard = testDataSeeder.seedActiveCard(account);
        deactivatedCard = testDataSeeder.seedDeactivatedCard(account);

    }


    @Test
    void unblockCardSuccessfully() throws Exception {
        m.put()
                .uri("/cards/unblock/" + blockedCard.getCardNumber())
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatus(HttpStatus.OK);

        Card unblockedCard = cardRepository.findById(blockedCard.getId()).orElse(null);
        assertThat(unblockedCard).isNotNull();
        assertThat(unblockedCard.getCardStatus()).isEqualTo(CardStatus.ACTIVATED);
    }
    @Test
    void unblockCardFailsForNonExistentCard() throws Exception {
        String nonExistentCardNumber = "9999999999999999";

        m.put()
                .uri("/cards/unblock/" + nonExistentCardNumber)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatus(HttpStatus.BAD_REQUEST);
    }
    @Test
    void unblockCardFailsForDeactivatedCard() throws Exception {
        m.put()
                .uri("/cards/unblock/" + deactivatedCard.getCardNumber())
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatus(HttpStatus.BAD_REQUEST);
    }
}
