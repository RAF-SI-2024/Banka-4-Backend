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

@Component
public class InvalidRouteFilter extends OncePerRequestFilter {

    private final HandlerMapping handlerMapping;

    public InvalidRouteFilter(@Qualifier("requestMappingHandlerMapping") HandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

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