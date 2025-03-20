package rs.banka4.user_service.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import rs.banka4.testlib.integration.DbEnabledTest;
import rs.banka4.user_service.integration.generator.UserGenerator;
import rs.banka4.user_service.utils.JwtUtil;

@SpringBootTest
@AutoConfigureMockMvc
@DbEnabledTest
public class GetAllClientsTest {

    @Autowired
    private MockMvcTester m;

    @Autowired
    private UserGenerator userGen;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objMapper;

    @Test
    void getAllClientsTest() throws Exception {
        userGen.createEmployee(x -> x);
        var toks = userGen.doEmployeeLogin("john.doe@example.com", "test");
        userGen.createClient(x -> x);

        m.get()
            .uri("/client/search?page=0&size=10")
            .header("Authorization", "Bearer " + toks.accessToken())
            .accept(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatusOk()
            .hasContentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            .bodyJson()
            .extractingPath("content")
            .asList()
            .satisfies(clients -> assertThat(clients).isNotEmpty());
    }
}
