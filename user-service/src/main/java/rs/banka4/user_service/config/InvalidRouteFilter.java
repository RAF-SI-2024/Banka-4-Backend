package rs.banka4.user_service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;
import rs.banka4.user_service.exceptions.RouteNotFound;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class InvalidRouteFilter extends OncePerRequestFilter {

    private final HandlerMapping handlerMapping;

    private final List<Pattern> WHITE_LIST_URL_PATTERNS = List.of(
            Pattern.compile("/docs/.*")
    );

    public InvalidRouteFilter(@Qualifier("requestMappingHandlerMapping") HandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        boolean isWhitelisted = WHITE_LIST_URL_PATTERNS.stream()
                .anyMatch(pattern -> pattern.matcher(requestURI).matches());

        if (!isWhitelisted) {
            try {
                if (handlerMapping.getHandler(request) == null) {
                    throw new RouteNotFound();
                }
            } catch (Exception e) {
                throw new RouteNotFound();
            }
        }

        filterChain.doFilter(request, response);
    }
}