package rs.banka4.user_service.config.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import rs.banka4.user_service.exceptions.BaseApiException;
import rs.banka4.user_service.exceptions.ErrorResponseHandler;

/**
 * Filter that handles exceptions thrown during the request processing by other filters.
 */
@Component
@RequiredArgsConstructor
public class ExceptionHandlingFilter extends OncePerRequestFilter {
    private final ObjectMapper objectMapper;
    private final ErrorResponseHandler errorResponseHandler;

    /**
     * Filters the incoming request to handle exceptions thrown by other filters. If a
     * {@link BaseApiException} is thrown, the filter catches it and sends an appropriate error
     * response back to the client.
     *
     * @param request the incoming HTTP request
     * @param response the HTTP response that will be sent back to the client
     * @param filterChain the chain of filters to be applied after this filter
     * @throws ServletException if a servlet-related error occurs during the filtering process
     * @throws IOException if an I/O error occurs during request or response handling
     */
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException,
        IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (BaseApiException ex) {
            response.setStatus(
                ex.getStatus()
                    .value()
            );
            response.setContentType("application/json");
            Map<String, Object> responseBody =
                errorResponseHandler.handleErrorResponse(ex)
                    .getBody();
            objectMapper.writeValue(response.getWriter(), responseBody);
        }
    }
}
