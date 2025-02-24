package rs.banka4.user_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import rs.banka4.user_service.exceptions.BaseApiException;
import rs.banka4.user_service.exceptions.ErrorResponseHandler;

import java.io.IOException;
import java.util.Map;

@Component
public class ExceptionHandlingFilter extends OncePerRequestFilter {

    private final ErrorResponseHandler errorResponseHandler;

    public ExceptionHandlingFilter(ErrorResponseHandler errorResponseHandler) {
        this.errorResponseHandler = errorResponseHandler;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (BaseApiException ex) {
            response.setStatus(ex.getStatus().value());
            response.setContentType("application/json");
            Map<String, Object> responseBody = errorResponseHandler.handleErrorResponse(ex).getBody();
            response.getWriter().write(new ObjectMapper().writeValueAsString(responseBody));
        }
    }
}