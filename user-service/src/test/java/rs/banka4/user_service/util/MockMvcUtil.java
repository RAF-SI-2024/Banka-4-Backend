package rs.banka4.user_service.util;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

public class MockMvcUtil {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    public MockMvcUtil(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    public <T> void performRequest(MockHttpServletRequestBuilder request, T expectedResponse)
        throws Exception {
        mockMvc.perform(
            request.with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer dummyToken")
        )
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));
    }

    public <T> void performPostRequest(
        MockHttpServletRequestBuilder request,
        T content,
        int expectedStatus
    ) throws Exception {
        mockMvc.perform(
            request.with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer dummyToken")
                .content(objectMapper.writeValueAsString(content))
        )
            .andExpect(status().is(expectedStatus));
    }
}
