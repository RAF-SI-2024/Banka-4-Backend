package rs.banka4.user_service.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Set;
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
import rs.banka4.user_service.domain.user.Privilege;
import rs.banka4.user_service.domain.user.Gender;
import rs.banka4.user_service.domain.user.client.dtos.CreateClientDto;
import rs.banka4.user_service.integration.generator.UserGenerator;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.utils.JwtUtil;

@SpringBootTest
@AutoConfigureMockMvc
@DbEnabledTest
public class CreateClientTest {

    @Autowired
    private MockMvcTester m;

    @Autowired
    private ObjectMapper objMapper;

    @Autowired
    private UserGenerator userGen;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private String accessToken;

    @BeforeEach
    void setUp() {
        userGen.createEmployee(x -> x);
        var toks = userGen.doEmployeeLogin("john.doe@example.com", "test");
        accessToken = toks.accessToken();
    }

    @AfterEach
    void tearDown() {
        clientRepository.deleteAll();
    }

    @Test
    void createClientSuccessfully() throws Exception {
        CreateClientDto createClientDto = new CreateClientDto(
                "Ognjen",
                "Jukic",
                LocalDate.of(1990, 5, 15),
                Gender.MALE,
                "mljubic9422112rn@raf.rs",
                "+1234567890",
                "123 Grove Street, City, Country",
                Set.of(Privilege.TRADE_STOCKS, Privilege.CONTRACTS)
        );

        String requestBody = objMapper.writeValueAsString(createClientDto);

        m.post()
                .uri("/client")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .assertThat()
                .hasStatus(HttpStatus.CREATED);
    }
}
