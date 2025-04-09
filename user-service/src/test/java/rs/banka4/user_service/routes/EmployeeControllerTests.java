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
import rs.banka4.rafeisen.common.dto.EmployeeResponseDto;
import rs.banka4.rafeisen.common.exceptions.ErrorResponseHandler;
import rs.banka4.user_service.config.filters.JwtAuthenticationFilter;
import rs.banka4.user_service.controller.EmployeeController;
import rs.banka4.user_service.domain.user.PrivilegesDto;
import rs.banka4.user_service.domain.user.employee.dtos.CreateEmployeeDto;
import rs.banka4.user_service.domain.user.employee.dtos.EmployeeDto;
import rs.banka4.user_service.domain.user.employee.dtos.UpdateEmployeeDto;
import rs.banka4.user_service.generator.EmployeeObjectMother;
import rs.banka4.user_service.service.abstraction.EmployeeService;
import rs.banka4.user_service.util.MockMvcUtil;

@WebMvcTest(EmployeeController.class)
@Import(EmployeeControllerTests.MockBeansConfig.class)
public class EmployeeControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EmployeeService employeeService;

    private MockMvcUtil mockMvcUtil;

    @BeforeEach
    void setUp() {
        mockMvcUtil = new MockMvcUtil(mockMvc, objectMapper);
    }

    @Test
    @WithMockUser(username = "user")
    void testGetPrivileges() throws Exception {
        PrivilegesDto privilegesDto = new PrivilegesDto(Collections.singletonList("ADMIN"));
        Mockito.when(employeeService.getPrivileges())
            .thenReturn(ResponseEntity.ok(privilegesDto));
        mockMvcUtil.performRequest(get("/employee/privileges"), privilegesDto);
    }

    @Test
    @WithMockUser(username = "user")
    void testGetMe() throws Exception {
        EmployeeResponseDto responseDto = EmployeeObjectMother.generateBasicEmployeeResponseDto();
        Mockito.when(employeeService.getMe(any(String.class)))
            .thenReturn(responseDto);
        mockMvcUtil.performRequest(get("/employee/me"), responseDto);
    }

    @Test
    @WithMockUser(username = "user")
    void testGetEmployeeById() throws Exception {
        UUID id = UUID.randomUUID();
        EmployeeResponseDto responseDto = EmployeeObjectMother.generateBasicEmployeeResponseDto();
        Mockito.when(employeeService.getEmployeeById(eq(id)))
            .thenReturn(responseDto);
        mockMvcUtil.performRequest(get("/employee/{id}", id), responseDto);
    }

    @Test
    @WithMockUser(username = "user")
    void testCreateEmployee() throws Exception {
        CreateEmployeeDto createEmployeeDto = EmployeeObjectMother.generateBasicCreateEmployeeDto();
        Mockito.doNothing()
            .when(employeeService)
            .createEmployee(any(CreateEmployeeDto.class));
        mockMvcUtil.performPostRequest(post("/employee"), createEmployeeDto, 201);
    }

    @Test
    @WithMockUser(username = "user")
    void testGetEmployees() throws Exception {
        EmployeeDto employeeDto = EmployeeObjectMother.generateBasicEmployeeDto();
        Page<EmployeeDto> page = new PageImpl<>(Collections.singletonList(employeeDto));
        Mockito.when(employeeService.getAll(any(), any(), any(), any(), any(PageRequest.class)))
            .thenReturn(ResponseEntity.ok(page));
        mockMvcUtil.performRequest(get("/employee/search"), page);
    }

    @Test
    @WithMockUser(username = "user")
    void testUpdateEmployee() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateEmployeeDto updateEmployeeDto = EmployeeObjectMother.generateBasicUpdateEmployeeDto();
        Mockito.doNothing()
            .when(employeeService)
            .updateEmployee(eq(id), any(UpdateEmployeeDto.class));
        mockMvc.perform(
            put("/employee/{id}", id).with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer dummyToken")
                .content(objectMapper.writeValueAsString(updateEmployeeDto))
        )
            .andExpect(status().isOk());
    }

    @TestConfiguration
    static class MockBeansConfig {
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
