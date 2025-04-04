package rs.banka4.user_service.routes;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import rs.banka4.rafeisen.common.exceptions.ErrorResponseHandler;
import rs.banka4.user_service.config.filters.JwtAuthenticationFilter;
import rs.banka4.user_service.controller.AuthController;
import rs.banka4.user_service.domain.auth.dtos.LoginDto;
import rs.banka4.user_service.domain.auth.dtos.LoginResponseDto;
import rs.banka4.user_service.domain.auth.dtos.LogoutDto;
import rs.banka4.user_service.domain.auth.dtos.RefreshTokenDto;
import rs.banka4.user_service.domain.auth.dtos.RefreshTokenResponseDto;
import rs.banka4.user_service.domain.auth.dtos.UserVerificationRequestDto;
import rs.banka4.user_service.service.abstraction.AuthService;
import rs.banka4.user_service.service.abstraction.ClientService;
import rs.banka4.user_service.service.abstraction.EmployeeService;

@WebMvcTest(AuthController.class)
@Import(AuthControllerTests.MockBeansConfig.class)
public class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AuthService authService;
    @Autowired
    private ClientService clientService;
    @Autowired
    private EmployeeService employeeService;

    @Test
    @WithMockUser
    void testEmployeeLogin() throws Exception {
        LoginDto loginDto = new LoginDto("waskeee@example.com", "password");
        LoginResponseDto responseDto = new LoginResponseDto("token", "refreshToken");
        Mockito.when(employeeService.login(any(LoginDto.class)))
            .thenReturn(responseDto);

        mockMvc.perform(
            post("/auth/employee/login").with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto))
        )
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
    }

    @Test
    @WithMockUser
    void testClientLogin() throws Exception {
        LoginDto loginDto = new LoginDto("mahd@example.com", "password");
        LoginResponseDto responseDto = new LoginResponseDto("token", "refreshToken");
        Mockito.when(clientService.login(any(LoginDto.class)))
            .thenReturn(responseDto);

        mockMvc.perform(
            post("/auth/client/login").with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto))
        )
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
    }

    @Test
    @WithMockUser
    void testRefreshToken() throws Exception {
        RefreshTokenDto refreshTokenDto = new RefreshTokenDto("sample-refresh-token");
        RefreshTokenResponseDto responseDto = new RefreshTokenResponseDto("new-refresh-token");
        Mockito.when(authService.refreshToken(eq(refreshTokenDto.refreshToken())))
            .thenReturn(responseDto);

        mockMvc.perform(
            post("/auth/refresh-token").with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenDto))
        )
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
    }

    @Test
    @WithMockUser
    void testLogout() throws Exception {
        LogoutDto logoutDto = new LogoutDto("sample-token");
        Mockito.doNothing()
            .when(authService)
            .logout(any(LogoutDto.class));

        mockMvc.perform(
            post("/auth/logout").with(SecurityMockMvcRequestPostProcessors.csrf())
                .header("Authorization", "Bearer dummyToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logoutDto))
        )
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testVerifyAccount() throws Exception {
        UserVerificationRequestDto requestDto =
            new UserVerificationRequestDto("oogilee@example.com", "verification-code");
        Mockito.doNothing()
            .when(authService)
            .verifyAccount(any(UserVerificationRequestDto.class));

        mockMvc.perform(
            post("/auth/verify").with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
        )
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testForgotPassword() throws Exception {
        String email = "user@example.com";
        Mockito.doNothing()
            .when(authService)
            .forgotPassword(eq(email));

        mockMvc.perform(
            post("/auth/forgot-password/" + email).with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk());
    }

    @TestConfiguration
    static class MockBeansConfig {
        @Bean
        public AuthService authService() {
            return Mockito.mock(AuthService.class);
        }

        @Bean
        public ClientService clientService() {
            return Mockito.mock(ClientService.class);
        }

        @Bean
        public EmployeeService employeeService() {
            return Mockito.mock(EmployeeService.class);
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
