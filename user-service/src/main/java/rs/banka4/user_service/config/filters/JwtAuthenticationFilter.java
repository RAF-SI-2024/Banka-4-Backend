package rs.banka4.user_service.config.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import rs.banka4.rafeisen.common.exceptions.jwt.NoJwtProvided;
import rs.banka4.rafeisen.common.security.AuthenticatedBankUserAuthentication;
import rs.banka4.rafeisen.common.security.AuthenticatedBankUserPrincipal;
import rs.banka4.rafeisen.common.utils.jwt.UnverifiedToken;
import rs.banka4.rafeisen.common.utils.jwt.VerifiedAccessToken;
import rs.banka4.user_service.config.WhiteListConfig;
import rs.banka4.user_service.service.abstraction.JwtService;


/**
 * Filter that checks if the request has a valid JWT token in the Authorization header.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    /**
     * Filters the incoming request to check if it contains a valid JWT token in the Authorization
     * header. If a valid token is found, the method sets the user authentication in the
     * {@link SecurityContextHolder}. If the token is invalid or not provided, an exception is
     * thrown.
     * <p>
     * This method is invoked once per request, ensuring that the request is authenticated before
     * reaching the intended endpoint.
     *
     * @param request the incoming HTTP request
     * @param response the HTTP response that will be sent back to the client
     * @param filterChain the chain of filters to be applied after this filter
     * @throws ServletException if a servlet-related error occurs during the filtering process
     * @throws IOException if an I/O error occurs during request or response handling
     */
    @Override
    public void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException,
        IOException {
        final var rawToken = parseAuthHeader(request.getHeader("Authorization"));

        /* TODO(arsen): remove */
        if (!WhiteListConfig.isWhitelisted(request.getRequestURI()) && rawToken == null)
            throw new NoJwtProvided();

        if (rawToken != null) {
            /* Provide authentication for the current user. */
            final var token = jwtService.parseToken(rawToken);
            if (!(token instanceof VerifiedAccessToken vat))
                /* Gave me a refresh token.. silly lad. */
                throw new NoJwtProvided();

            final var authentication =
                new AuthenticatedBankUserAuthentication(
                    new AuthenticatedBankUserPrincipal(token.getRole(), token.getSub()),
                    rawToken.rawJwt(),
                    vat.getPrivileges()
                );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext()
                .setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private static UnverifiedToken parseAuthHeader(String header) {
        if (header == null) return null;

        final var bearerPrefix = "bearer ";
        try {
            if (
                !header.substring(0, bearerPrefix.length())
                    .equalsIgnoreCase(bearerPrefix)
            ) return null;

            return new UnverifiedToken(header.substring(bearerPrefix.length()));
        } catch (IndexOutOfBoundsException ignored) {
            return null;
        }
    }
}
