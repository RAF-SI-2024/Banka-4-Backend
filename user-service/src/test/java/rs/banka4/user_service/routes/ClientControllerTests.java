package rs.banka4.user_service.routes;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import rs.banka4.user_service.controller.ClientController;
import rs.banka4.user_service.domain.user.client.dtos.ClientDto;
import rs.banka4.user_service.domain.user.client.dtos.CreateClientDto;
import rs.banka4.user_service.domain.user.client.dtos.UpdateClientDto;
import rs.banka4.user_service.generator.ClientObjectMother;
import rs.banka4.user_service.service.abstraction.ClientService;
import rs.banka4.user_service.service.impl.CustomUserDetailsService;
import rs.banka4.user_service.util.MockMvcUtil;
import rs.banka4.user_service.utils.JwtUtil;

@WebMvcTest(ClientController.class)
@Import(ClientControllerTests.MockBeansConfig.class)
public class ClientControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ClientService clientService;

    private MockMvcUtil mockMvcUtil;

    @BeforeEach
    void setUp() {
        mockMvcUtil = new MockMvcUtil(mockMvc, objectMapper);
    }

    @Test
    @WithMockUser(username = "user")
    void testGetMe() throws Exception {
        ClientDto responseDto = ClientObjectMother.generateBasicClientDto();
        Mockito.when(clientService.getMe(any(String.class)))
            .thenReturn(responseDto);
        mockMvcUtil.performRequest(get("/client/me"), responseDto);
    }

    @Test
    @WithMockUser(username = "user")
    void testGetClientById() throws Exception {
        UUID id = UUID.randomUUID();
        ClientDto responseDto = ClientObjectMother.generateBasicClientDto();
        Mockito.when(clientService.getClientById(eq(id)))
            .thenReturn(responseDto);
        mockMvcUtil.performRequest(get("/client/{id}", id), responseDto);
    }

    @Test
    @WithMockUser(username = "user")
    void testCreateClient() throws Exception {
        CreateClientDto createClientDto = ClientObjectMother.generateBasicCreateClientDto();
        Mockito.doNothing()
            .when(clientService)
            .createClient(any(CreateClientDto.class));
        mockMvcUtil.performPostRequest(post("/client"), createClientDto, 201);
    }

    @Test
    @WithMockUser(username = "user")
    void testGetClients() throws Exception {
        ClientDto clientDto = ClientObjectMother.generateBasicClientDto();
        Page<ClientDto> page = new PageImpl<>(Collections.singletonList(clientDto));
        Mockito.when(
            clientService.getClients(any(), any(), any(), any(), any(), any(PageRequest.class))
        )
            .thenReturn(ResponseEntity.ok(page));
        mockMvcUtil.performRequest(get("/client/search"), page);
    }

    @Test
    @WithMockUser(username = "user")
    void testUpdateClient() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateClientDto updateClientDto = ClientObjectMother.generateBasicUpdateClientDto();
        Mockito.doNothing()
            .when(clientService)
            .updateClient(eq(id), any(UpdateClientDto.class));
        mockMvc.perform(
            put("/client/{id}", id).with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer dummyToken")
                .content(objectMapper.writeValueAsString(updateClientDto))
        )
            .andExpect(status().isNoContent());
    }

    @TestConfiguration
    static class MockBeansConfig {
        @Bean
        public ClientService clientService() {
            return Mockito.mock(ClientService.class);
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
