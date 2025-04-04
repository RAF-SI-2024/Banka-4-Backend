package rs.banka4.user_service.routes;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import rs.banka4.rafeisen.common.exceptions.ErrorResponseHandler;
import rs.banka4.user_service.config.filters.JwtAuthenticationFilter;
import rs.banka4.user_service.controller.TotpController;
import rs.banka4.user_service.domain.authenticator.db.SentCode;
import rs.banka4.user_service.domain.authenticator.dtos.RegenerateAuthenticatorResponseDto;
import rs.banka4.user_service.service.impl.TotpServiceImpl;
import rs.banka4.user_service.util.MockMvcUtil;

@WebMvcTest(TotpController.class)
@Import(TotpControllerTests.MockBeansConfig.class)
public class TotpControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TotpServiceImpl totpService;

    private MockMvcUtil mockMvcUtil;

    @BeforeEach
    void setUp() {
        mockMvcUtil = new MockMvcUtil(mockMvc, objectMapper);
    }

    @Test
    @WithMockUser(username = "user")
    void testRegenerateAuthenticator() throws Exception {
        RegenerateAuthenticatorResponseDto responseDto =
            new RegenerateAuthenticatorResponseDto("url", "newSecret");
        Mockito.when(totpService.regenerateSecret(any()))
            .thenReturn(responseDto);
        mockMvcUtil.performRequest(get("/verify/regenerate-authenticator"), responseDto);
    }

    @Test
    @WithMockUser(username = "user")
    void testVerifyNewAuthenticator() throws Exception {
        SentCode sentCode = new SentCode("123456");
        Mockito.doNothing()
            .when(totpService)
            .verifyNewAuthenticator(any(), any(String.class));
        mockMvcUtil.performPostRequest(post("/verify/verify-new-authenticator"), sentCode, 200);
    }

    @Test
    @WithMockUser(username = "user")
    void testVerifyCode() throws Exception {
        SentCode sentCode = new SentCode("123456");
        Mockito.when(totpService.validate(any(String.class), any(String.class)))
            .thenReturn(true);
        mockMvcUtil.performPostRequest(post("/verify/validate"), sentCode, 200);
    }

    @TestConfiguration
    static class MockBeansConfig {
        @Bean
        public TotpServiceImpl totpService() {
            return Mockito.mock(TotpServiceImpl.class);
        }

        @Bean
        public JwtAuthenticationFilter jwtAuthenticationFilter() {
            return new NoopJwtAuthenticationFilter();
        }

        @Bean
        public ErrorResponseHandler errorResponseHandler() {
            return new ErrorResponseHandler();
        }
    }
}
