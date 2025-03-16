package rs.banka4.user_service.config.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;
import rs.banka4.user_service.config.WhiteListConfig;
import rs.banka4.user_service.exceptions.RouteNotFound;

import java.io.IOException;

/**
 * Filter that checks if the incoming request is for a valid route.
 * <p>
 * This filter ensures that only valid routes are processed by the application.
 * The filter uses a {@link HandlerMapping} to determine if the request matches any registered route.
 */
@Component
public class InvalidRouteFilter extends OncePerRequestFilter {

    private final HandlerMapping handlerMapping;

    public InvalidRouteFilter(@Qualifier("requestMappingHandlerMapping") HandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    /**
     * Filters the incoming request to check if it matches any registered route.
     * If the route is not found and the request URI is not whitelisted, a {@link RouteNotFound} exception is thrown.
     *
     * @param request the incoming HTTP request
     * @param response the HTTP response that will be sent back to the client
     * @param filterChain the chain of filters to be applied after this filter
     * @throws ServletException if a servlet-related error occurs during the filtering process
     * @throws IOException if an I/O error occurs during request or response handling
     */
    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        if (!WhiteListConfig.isWhitelisted(requestURI)) {
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