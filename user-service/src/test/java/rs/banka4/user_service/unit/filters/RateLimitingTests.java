package rs.banka4.user_service.unit.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import rs.banka4.user_service.config.RateLimitingFilter;
import rs.banka4.user_service.exceptions.RateLimitExceeded;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RateLimitingTests {

    private RateLimitingFilter rateLimitingFilter;

    @BeforeEach
    public void setUp() {
        rateLimitingFilter = new RateLimitingFilter();
    }

    @Test
    public void testRateLimitingAllowsRequest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = new MockFilterChain();

        request.setRemoteAddr("127.0.0.1");

        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        assertTrue(response.getStatus() == 200 || response.getStatus() == 0);
    }

    @Test
    public void testRateLimitingBlocksRequest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setRemoteAddr("127.0.0.1");

        // Consume all tokens
        for (int i = 0; i < 100; i++) {
            rateLimitingFilter.doFilterInternal(request, response, new MockFilterChain());
        }

        // Next request should be blocked
        MockHttpServletResponse blockedResponse = new MockHttpServletResponse();
        assertThrows(RateLimitExceeded.class, () -> {
            rateLimitingFilter.doFilterInternal(request, blockedResponse, new MockFilterChain());
        });
    }

}
