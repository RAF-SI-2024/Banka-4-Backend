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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import rs.banka4.user_service.controller.LoanController;
import rs.banka4.user_service.domain.loan.dtos.LoanApplicationDto;
import rs.banka4.user_service.domain.loan.dtos.LoanFilterDto;
import rs.banka4.user_service.domain.loan.dtos.LoanInformationDto;
import rs.banka4.user_service.generator.LoanObjectMother;
import rs.banka4.user_service.service.abstraction.LoanService;
import rs.banka4.user_service.service.impl.CustomUserDetailsService;
import rs.banka4.user_service.util.MockMvcUtil;
import rs.banka4.user_service.utils.JwtUtil;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(LoanController.class)
@Import(LoanControllerTests.MockBeansConfig.class)
public class LoanControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private LoanService loanService;

    private MockMvcUtil mockMvcUtil;

    @BeforeEach
    void setUp() {
        mockMvcUtil = new MockMvcUtil(mockMvc, objectMapper);
    }

    @Test
    @WithMockUser(username = "user")
    void testCreateLoanApplication() throws Exception {
        LoanApplicationDto loanApplicationDto = LoanObjectMother.generateLoanApplicationDto();
        Mockito.doNothing().when(loanService).createLoanApplication(any(LoanApplicationDto.class), anyString());
        mockMvcUtil.performPostRequest(post("/loans"), loanApplicationDto);
    }

    @Test
    @WithMockUser(username = "user")
    void testGetAllLoans() throws Exception {
        LoanInformationDto loanInformationDto = LoanObjectMother.generateLoanInformationDto();
        Page<LoanInformationDto> page = new PageImpl<>(Collections.singletonList(loanInformationDto));
        Mockito.when(loanService.getAllLoans(any(PageRequest.class),any(LoanFilterDto.class))).thenReturn(ResponseEntity.ok(page));

        mockMvcUtil.performRequest(get("/loans/search"), page);
    }

    @TestConfiguration
    static class MockBeansConfig {
        @Bean
        public LoanService loanService() {
            return Mockito.mock(LoanService.class);
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
