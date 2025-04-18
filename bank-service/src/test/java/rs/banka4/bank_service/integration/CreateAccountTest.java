package rs.banka4.bank_service.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import rs.banka4.bank_service.domain.account.db.Account;
import rs.banka4.bank_service.domain.account.dtos.AccountClientIdDto;
import rs.banka4.bank_service.domain.account.dtos.CreateAccountDto;
import rs.banka4.bank_service.domain.user.client.db.Client;
import rs.banka4.bank_service.integration.generator.UserGenerator;
import rs.banka4.bank_service.integration.seeder.TestDataSeeder;
import rs.banka4.bank_service.repositories.AccountRepository;
import rs.banka4.bank_service.repositories.ClientRepository;
import rs.banka4.bank_service.service.abstraction.JwtService;
import rs.banka4.testlib.integration.DbEnabledTest;

@SpringBootTest
@AutoConfigureMockMvc
@DbEnabledTest
public class CreateAccountTest {

    @Autowired
    private MockMvcTester m;

    @Autowired
    private UserGenerator userGen;

    @Qualifier("jwtServiceImpl")
    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TestDataSeeder testDataSeeder;

    @Autowired
    private ClientRepository clientRepository;

    private String accessToken;

    @BeforeEach
    void setUp() {
        userGen.createEmployee(x -> x);
        userGen.createClient(x -> x.email("salko.dinamitas@gmail.com"));
        var toks = userGen.doEmployeeLogin("john.doe@example.com", "test");
        accessToken = toks.accessToken();
    }

    @Test
    void createAccountSuccessfully() throws Exception {
        var currency = testDataSeeder.seedCurrency();
        Client client =
            clientRepository.findByEmail("salko.dinamitas@gmail.com")
                .get();
        if (
            clientRepository.findByEmail("salko.dinamitas@gmail.com")
                .isPresent()
        ) {
            clientRepository.findByEmail("salko.dinamitas@gmail.com")
                .get();
        }
        AccountClientIdDto accountClientIdDto =
            new AccountClientIdDto(
                client.getId(),
                client.getFirstName(),
                client.getLastName(),
                client.getDateOfBirth(),
                client.getGender(),
                client.getEmail(),
                client.getPhone(),
                client.getAddress(),
                client.getPrivileges()
            );
        CreateAccountDto createAccountDto =
            new CreateAccountDto(
                accountClientIdDto,
                null,
                BigDecimal.valueOf(5000),
                currency,
                true
            );

        String requestBody = objMapper.writeValueAsString(createAccountDto);

        m.post()
            .uri("/account")
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(requestBody)
            .assertThat()
            .hasStatus(HttpStatus.CREATED);

        Account createdAccount =
            accountRepository.findAll()
                .stream()
                .findFirst()
                .orElse(null);
        assertThat(createdAccount).isNotNull();
        assertThat(createdAccount.getAvailableBalance()).isEqualByComparingTo(
            BigDecimal.valueOf(5000)
        );
        assertThat(createdAccount.getCurrency()).isEqualTo(currency);
    }
}
