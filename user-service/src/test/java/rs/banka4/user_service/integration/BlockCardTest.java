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
import rs.banka4.user_service.integration.generator.UserGenerator;
import rs.banka4.user_service.integration.seeder.TestDataSeeder;
import rs.banka4.user_service.repositories.CardRepository;
import rs.banka4.user_service.utils.JwtUtil;

@SpringBootTest
@AutoConfigureMockMvc
@DbEnabledTest
public class BlockCardTest {

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
    private TestDataSeeder testDataSeeder;

    private Card card;
    private String accessToken;

    @BeforeEach
    void setUp() {
        Account account = testDataSeeder.seedAccount();
        card = testDataSeeder.seedCard(account);

        userGen.createEmployee(x -> x);
        var toks = userGen.doEmployeeLogin("john.doe@example.com", "test");
        accessToken = toks.accessToken();
    }

    @Test
    void blockCardSuccessfully() throws Exception {
        m.put()
                .uri("/cards/block/" + card.getCardNumber())
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatus(HttpStatus.OK);

        Card blockedCard = cardRepository.findById(card.getId()).orElse(null);
        assertThat(blockedCard).isNotNull();
        assertThat(blockedCard.getCardStatus()).isEqualTo(CardStatus.BLOCKED);
    }
    @Test
    void blockCardFailsForNonExistentCard() throws Exception {
        String nonExistentCardNumber = "9999999999999999";

        m.put()
                .uri("/cards/block/" + nonExistentCardNumber)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatus(HttpStatus.NOT_FOUND);
    }
    @Test
    void blockCardFailsForDeactivatedCard() throws Exception {
        m.put()
                .uri("/cards/block/" + deactivatedCard.getCardNumber())
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatus(HttpStatus.BAD_REQUEST);
    }
}
