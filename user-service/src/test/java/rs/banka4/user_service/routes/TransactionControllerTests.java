package rs.banka4.user_service.routes;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

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
import org.springframework.test.web.servlet.MockMvc;
import rs.banka4.rafeisen.common.exceptions.ErrorResponseHandler;
import rs.banka4.user_service.config.filters.JwtAuthenticationFilter;
import rs.banka4.user_service.controller.TransactionController;
import rs.banka4.user_service.domain.transaction.dtos.CreatePaymentDto;
import rs.banka4.user_service.domain.transaction.dtos.TransactionDto;
import rs.banka4.user_service.generator.TransactionObjectMother;
import rs.banka4.user_service.service.abstraction.JwtService;
import rs.banka4.user_service.service.abstraction.TransactionService;
import rs.banka4.user_service.util.MockMvcUtil;

@WebMvcTest(TransactionController.class)
@Import(TransactionControllerTests.MockBeansConfig.class)
public class TransactionControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TransactionService transactionService;

    private MockMvcUtil mockMvcUtil;

    @BeforeEach
    void setUp() {
        mockMvcUtil = new MockMvcUtil(mockMvc, objectMapper);
    }

    @Test
    @WithMockUser(username = "user")
    void testGetAllTransactions() throws Exception {
        TransactionDto transactionDto = TransactionObjectMother.generateBasicTransactionDto();
        Page<TransactionDto> page = new PageImpl<>(Collections.singletonList(transactionDto));
        Mockito.when(
            transactionService.getAllTransactionsForClient(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(PageRequest.class)
            )
        )
            .thenReturn(page);
        mockMvcUtil.performRequest(get("/transaction/search"), page);
    }

    @Test
    @WithMockUser(username = "user")
    void testGetTransaction() throws Exception {
        UUID id = UUID.randomUUID();
        TransactionDto transactionDto = TransactionObjectMother.generateBasicTransactionDto();
        Mockito.when(transactionService.getTransactionById(any(String.class), eq(id)))
            .thenReturn(transactionDto);
        mockMvcUtil.performRequest(get("/transaction/{id}", id), transactionDto);
    }

    @Test
    @WithMockUser(username = "user")
    void testCreateTransaction() throws Exception {
        CreatePaymentDto createPaymentDto = TransactionObjectMother.generateBasicCreatePaymentDto();
        TransactionDto transactionDto = TransactionObjectMother.generateBasicTransactionDto();
        Mockito.when(transactionService.createTransaction(any(), any(CreatePaymentDto.class)))
            .thenReturn(transactionDto);
        mockMvcUtil.performPostRequest(post("/transaction/payment"), createPaymentDto, 201);
    }

    @TestConfiguration
    static class MockBeansConfig {
        @Bean
        public TransactionService transactionService() {
            return Mockito.mock(TransactionService.class);
        }

        @Bean
        public JwtService jwtUtil() {
            return Mockito.mock(JwtService.class);
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
