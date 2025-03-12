package rs.banka4.user_service.integration;

import static org.assertj.core.api.Assertions.*;

import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import rs.banka4.testlib.integration.DbEnabledTest;
import rs.banka4.user_service.integration.generator.UserGenerator;
import rs.banka4.user_service.repositories.ClientRepository;

@SpringBootTest
@AutoConfigureMockMvc
@DbEnabledTest
public class EmployeeCRUDTest {
    @Autowired
    private MockMvcTester m;
    @Autowired
    private UserGenerator userGen;

    @Test
    void emptyOutOfTheBox(@Autowired ClientRepository clientRepo) {
        /* Verify that there's no clients OOTB.  */
        assertThat(clientRepo.count()).isEqualTo(0);
    }

    @Test
    void listEmployeesTest() throws Exception {
        userGen.createEmployee(x -> x);
        var toks = userGen.doEmployeeLogin("john.doe@example.com", "test");

        IntStream.range(0, 5)
            .mapToObj("employee%d"::formatted)
            .forEach(username -> userGen.createEmployee(
                c -> c
                    .email(username + "@example.com")
                    .username(username)
            ));

        m.get()
            .accept(MediaType.APPLICATION_JSON)
            .uri("/employee/search")
            .queryParam("size", "1")
            .queryParam("page", "3")
            .header("Authorization", "Bearer %s".formatted(toks.accessToken()))
            .assertThat()
            .hasStatusOk()
            .hasContentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            .bodyJson()
            /* Be lenient.  We're testing that the array is size one, and that
             * we're looking at page three.
             */
            .isLenientlyEqualTo("testdata/EmployeeCRUDTest_listEmployeeTest_1.json");
    }

}
