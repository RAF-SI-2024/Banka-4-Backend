package rs.banka4.user_service.routes;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import rs.banka4.user_service.controller.ClientContactController;
import rs.banka4.user_service.domain.user.client.dtos.ClientContactDto;
import rs.banka4.user_service.domain.user.client.dtos.ClientContactRequest;
import rs.banka4.user_service.generator.ClientObjectMother;
import rs.banka4.user_service.service.abstraction.ClientContactService;
import rs.banka4.user_service.service.impl.CustomUserDetailsService;
import rs.banka4.user_service.util.MockMvcUtil;
import rs.banka4.user_service.utils.JwtUtil;

@WebMvcTest(ClientContactController.class)
@Import(ClientContactControllerTests.MockBeansConfig.class)
public class ClientContactControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ClientContactService clientContactService;

    private MockMvcUtil mockMvcUtil;

    @BeforeEach
    void setUp() {
        mockMvcUtil = new MockMvcUtil(mockMvc, objectMapper);
    }

    @Test
    @WithMockUser(username = "user")
    void testGetAllClientContacts() throws Exception {
        ClientContactDto clientContactDto = ClientObjectMother.generateBasicClientContactDto();
        Page<ClientContactDto> page = new PageImpl<>(Collections.singletonList(clientContactDto));
        Mockito.when(clientContactService.getAllClientContacts(any(), any(PageRequest.class)))
            .thenReturn(page);
        mockMvcUtil.performRequest(get("/client-contact"), page);
    }

    @Test
    @WithMockUser(username = "user")
    void testGetSpecificClientContact() throws Exception {
        UUID id = UUID.randomUUID();
        ClientContactDto clientContactDto = ClientObjectMother.generateBasicClientContactDto();
        Mockito.when(clientContactService.getSpecificClientContact(any(), eq(id)))
            .thenReturn(clientContactDto);
        mockMvcUtil.performRequest(get("/client-contact/{id}", id), clientContactDto);
    }

    @Test
    @WithMockUser(username = "user")
    void testCreateClientContact() throws Exception {
        ClientContactRequest clientContactRequest =
            ClientObjectMother.generateBasicClientContactRequest();
        Mockito.doNothing()
            .when(clientContactService)
            .createClientContact(any(), any(ClientContactRequest.class));
        mockMvcUtil.performPostRequest(post("/client-contact"), clientContactRequest, 201);
    }

    @Test
    @WithMockUser(username = "user")
    void testUpdateClientContact() throws Exception {
        UUID id = UUID.randomUUID();
        ClientContactRequest clientContactRequest =
            ClientObjectMother.generateBasicClientContactRequest();
        Mockito.doNothing()
            .when(clientContactService)
            .updateClientContact(any(), eq(id), any(ClientContactRequest.class));
        mockMvc.perform(
            put("/client-contact/{id}", id).with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType("application/json")
                .header("Authorization", "Bearer dummyToken")
                .content(objectMapper.writeValueAsString(clientContactRequest))
        )
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user")
    void testDeleteClientContact() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.doNothing()
            .when(clientContactService)
            .deleteClientContact(any(), eq(id));
        mockMvc.perform(
            delete("/client-contact/{id}", id).with(SecurityMockMvcRequestPostProcessors.csrf())
                .header("Authorization", "Bearer dummyToken")
        )
            .andExpect(status().isOk());
    }

    @TestConfiguration
    static class MockBeansConfig {
        @Bean
        public ClientContactService clientContactService() {
            return Mockito.mock(ClientContactService.class);
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
