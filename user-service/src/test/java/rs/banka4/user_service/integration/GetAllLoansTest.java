package rs.banka4.user_service.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import rs.banka4.testlib.integration.DbEnabledTest;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.loan.db.Loan;
import rs.banka4.user_service.integration.generator.UserGenerator;
import rs.banka4.user_service.integration.seeder.TestDataSeeder;
import rs.banka4.user_service.repositories.LoanRepository;
import rs.banka4.user_service.utils.JwtUtil;

@SpringBootTest
@AutoConfigureMockMvc
@DbEnabledTest
public class GetAllLoansTest {

    @Autowired
    private MockMvcTester m;

    @Autowired
    private UserGenerator userGen;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objMapper;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private TestDataSeeder testDataSeeder;

    private List<Loan> loans;
    private String accessToken;

    @BeforeEach
    void setUp() {
        Account account = testDataSeeder.seedAccount();
        loans = testDataSeeder.seedLoans(account);

        userGen.createEmployee(x -> x);
        var toks = userGen.doEmployeeLogin("john.doe@example.com", "test");
        accessToken = toks.accessToken();
    }

    @Test
    void getAllLoansSuccessfully() throws Exception {
        m.get()
            .uri("/loans/search?page=0&size=10")
            .header("Authorization", "Bearer " + accessToken)
            .accept(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatusOk()
            .hasContentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            .bodyJson()
            .extractingPath("content")
            .asList()
            .satisfies(loansList -> assertThat(loansList).isNotEmpty());
    }
}
