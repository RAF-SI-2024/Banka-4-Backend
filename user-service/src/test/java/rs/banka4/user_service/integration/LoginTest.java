package rs.banka4.user_service.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import com.fasterxml.jackson.databind.ObjectMapper;

import rs.banka4.user_service.integration.generator.UserGenerator;
import rs.banka4.user_service.integration.utils.DbEnabledTest;
import rs.banka4.user_service.utils.JwtUtil;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc
@DbEnabledTest
public class LoginTest {
    @Autowired
    private MockMvcTester m;
    @Autowired
    private UserGenerator userGen;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private ObjectMapper objMapper;

    @Test
    void loginTest() throws Exception {
        userGen.createEmployee(x -> x);

        m.post()
            .uri("/auth/employee/login")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content("""
                     {
                       "email": "john.doe@example.com",
                       "password": "test"
                     }
                     """)

            .assertThat()
            .hasStatusOk()
            .hasContentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            .bodyJson()
            .extractingPath("refreshToken")
            .asString()
            .satisfies(rt ->
                assertThat(jwtUtil.extractUsername(rt))
                    .isEqualTo("john.doe@example.com"));
    }

    @Test
    void refreshTokenTestEmployee() throws Exception {
        userGen.createEmployee(x -> x);
        var toks = userGen.doEmployeeLogin("john.doe@example.com", "test");

        m.post()
            .uri("/auth/refresh-token")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(objMapper.writeValueAsString(
                Map.of("refreshToken", toks.refreshToken())
            ))

            .assertThat()
            .hasStatusOk()
            .hasContentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            .bodyJson()
            .extractingPath("accessToken")
            .asString()
            .satisfies(rt ->
                assertThat(jwtUtil.extractUsername(rt))
                    .isEqualTo("john.doe@example.com"));
    }
}
