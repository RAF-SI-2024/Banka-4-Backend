package rs.banka4.user_service.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MockMvcUtil {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    public MockMvcUtil(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    public <T> void performRequest(MockHttpServletRequestBuilder request, T expectedResponse) throws Exception {
        mockMvc.perform(request
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer dummyToken"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));
    }

    public <T> void performPostRequest(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request, T content) throws Exception {
        mockMvc.perform(request
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer dummyToken")
                        .content(objectMapper.writeValueAsString(content)))
                .andExpect(status().isCreated());
    }
}