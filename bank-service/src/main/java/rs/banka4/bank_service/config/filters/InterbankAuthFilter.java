package rs.banka4.bank_service.config.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import rs.banka4.bank_service.security.InterbankAuthentication;
import rs.banka4.bank_service.tx.config.InterbankConfig;

/**
 * Filter that authenticates other banks to access our routes.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InterbankAuthFilter extends OncePerRequestFilter {
    private final InterbankConfig interbankConfig;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException,
        IOException {
        final var apiKeyStr = request.getHeader("X-Api-Key");
        if (apiKeyStr == null) {
            /* Nothing to do. */
            filterChain.doFilter(request, response);
            return;
        }

        final var resolution = interbankConfig.resolveApiKey(apiKeyStr);
        log.trace("Verifying API key {}: {}", apiKeyStr, resolution);
        resolution.map(InterbankAuthentication::new)
            .ifPresent(SecurityContextHolder.getContext()::setAuthentication);

        filterChain.doFilter(request, response);
    }
}
