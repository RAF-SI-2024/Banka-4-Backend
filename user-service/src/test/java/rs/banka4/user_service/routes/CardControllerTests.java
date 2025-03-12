package rs.banka4.user_service.routes;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import rs.banka4.user_service.controller.CardController;
import rs.banka4.user_service.domain.card.dtos.CreateCardDto;
import rs.banka4.user_service.exceptions.authenticator.NotValidTotpException;
import rs.banka4.user_service.generator.CardObjectMother;
import rs.banka4.user_service.service.abstraction.CardService;
import rs.banka4.user_service.service.impl.CustomUserDetailsService;
import rs.banka4.user_service.util.MockMvcUtil;
import rs.banka4.user_service.utils.JwtUtil;

@WebMvcTest(CardController.class)
@Import(CardControllerTests.MockBeansConfig.class)
public class CardControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CardService cardService;

    private MockMvcUtil mockMvcUtil;

    @BeforeEach
    void setUp() {
        mockMvcUtil = new MockMvcUtil(mockMvc, objectMapper);
    }

    /**
     * Test the successful creation of an authorized card. The controller delegates to the service
     * and returns HTTP 201 Created.
     */
    @Test
    @WithMockUser(username = "client")
    void testCreateAuthorizedCard_Success() throws Exception {
        // Prepare a valid CreateCardDto using the Object Mother.
        CreateCardDto createCardDto = CardObjectMother.validRequest();

        // Stub the service to do nothing (simulate success).
        Mockito.doNothing()
            .when(cardService)
            .createAuthorizedCard(any(Authentication.class), eq(createCardDto));

        // Use the custom MockMvcUtil to perform a POST request.
        mockMvcUtil.performPostRequest(post("/cards/create"), createCardDto, 201);
    }

    /**
     * Test the create endpoint with invalid input. Expect a 403 Forbidden due to missing required
     * fields.
     */
    @Test
    @WithMockUser(username = "client")
    void testCreateAuthorizedCard_InvalidInput() throws Exception {
        // Create an invalid DTO (e.g., missing account number and OTP code).
        CreateCardDto invalidRequest = new CreateCardDto("", null, null);

        mockMvc.perform(
            post("/cards/create").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer dummyToken")
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
            .andExpect(status().isForbidden());
    }

    /**
     * Test the create endpoint when the service throws an exception (for example, due to an invalid
     * TOTP), expecting a 403 is Forbidden.
     */
    @Test
    @WithMockUser(username = "client")
    void testCreateAuthorizedCard_ServiceException() throws Exception {
        CreateCardDto createCardDto = CardObjectMother.validRequest();

        // Simulate the service throwing a NotValidTotpException.
        Mockito.doThrow(new NotValidTotpException())
            .when(cardService)
            .createAuthorizedCard(any(Authentication.class), eq(createCardDto));

        mockMvc.perform(
            post("/cards/create").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer dummyToken")
                .content(objectMapper.writeValueAsString(createCardDto))
        )
            .andExpect(status().isForbidden());
    }

    @TestConfiguration
    static class MockBeansConfig {
        @Bean
        public CardService cardService() {
            return Mockito.mock(CardService.class);
        }

        @Bean
        public JwtUtil jwtUtil() {
            return Mockito.mock(JwtUtil.class);
        }

        @Bean
        public CustomUserDetailsService customUserDetailsService() {
            return Mockito.mock(CustomUserDetailsService.class);
        }
    }
}
