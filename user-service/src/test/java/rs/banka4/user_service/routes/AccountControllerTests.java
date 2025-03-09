package rs.banka4.user_service.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import rs.banka4.user_service.controller.AccountController;
import rs.banka4.user_service.domain.account.dtos.AccountDto;
import rs.banka4.user_service.domain.account.dtos.CreateAccountDto;
import rs.banka4.user_service.generator.AccountObjectMother;
import rs.banka4.user_service.service.abstraction.AccountService;
import rs.banka4.user_service.service.impl.CustomUserDetailsService;
import rs.banka4.user_service.utils.JwtUtil;
import rs.banka4.user_service.util.MockMvcUtil;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(AccountController.class)
@Import(AccountControllerTests.MockBeansConfig.class)
public class AccountControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AccountService accountService;

    private MockMvcUtil mockMvcUtil;

    @BeforeEach
    void setUp() {
        mockMvcUtil = new MockMvcUtil(mockMvc, objectMapper);
    }

    @Test
    @WithMockUser(username = "user")
    void testGetAll() throws Exception {
        AccountDto accountDto = AccountObjectMother.generateBasicAccountDto();
        Page<AccountDto> page = new PageImpl<>(Collections.singletonList(accountDto));
        Mockito.when(accountService.getAll(any(), any(), any(), any(), any(PageRequest.class))).thenReturn(ResponseEntity.ok(page));
        mockMvcUtil.performRequest(get("/account/search"), page);
    }

    @Test
    @WithMockUser(username = "user")
    void testGetAccount() throws Exception {
        UUID id = UUID.randomUUID();
        AccountDto accountDto = AccountObjectMother.generateBasicAccountDto();
        Mockito.when(accountService.getAccount(any(String.class), eq(id.toString()))).thenReturn(accountDto);
        mockMvcUtil.performRequest(get("/account/{id}", id), accountDto);
    }

    @Test
    @WithMockUser(username = "user")
    void testGetAccountsForClient() throws Exception {
        Set<AccountDto> accounts = Collections.singleton(AccountObjectMother.generateBasicAccountDto());
        Mockito.when(accountService.getAccountsForClient(any(String.class))).thenReturn(accounts);
        mockMvcUtil.performRequest(get("/account"), accounts);
    }

    @Test
    @WithMockUser(username = "user")
    void testCreateAccount() throws Exception {
        CreateAccountDto createAccountDto = AccountObjectMother.generateBasicCreateAccountDto();
        Mockito.doNothing().when(accountService).createAccount(any(CreateAccountDto.class), any(String.class));
        mockMvcUtil.performPostRequest(post("/account"), createAccountDto);
    }

    @TestConfiguration
    static class MockBeansConfig {
        @Bean
        public AccountService accountService() {
            return Mockito.mock(AccountService.class);
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