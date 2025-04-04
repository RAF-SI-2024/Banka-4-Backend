package rs.banka4.stock_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;
import rs.banka4.rafeisen.common.exceptions.jwt.ExpiredJwt;
import rs.banka4.rafeisen.common.exceptions.jwt.NoJwtProvided;
import rs.banka4.rafeisen.common.security.AuthenticatedBankUserAuthentication;
import rs.banka4.rafeisen.common.security.AuthenticatedBankUserPrincipal;
import rs.banka4.rafeisen.common.utils.jwt.JwtParseFailed;
import rs.banka4.rafeisen.common.utils.jwt.JwtUtil;
import rs.banka4.rafeisen.common.utils.jwt.UnverifiedToken;
import rs.banka4.rafeisen.common.utils.jwt.VerifiedAccessToken;
import rs.banka4.rafeisen.common.utils.jwt.VerifiedToken;

/**
 * Parse a header token and register it with the {@link SecurityContextHolder}.
 */
@Service
@RequiredArgsConstructor
public class BearerTokenParsingFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(BearerTokenParsingFilter.class);
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException,
        IOException {
        final var providedToken = parseAuthHeader(request.getHeader("Authorization"));
        if (providedToken != null) {
            LOGGER.trace("Judging a token");
            VerifiedToken parsedToken;
            try {
                parsedToken = jwtUtil.parseToken(providedToken);
            } catch (JwtParseFailed e) {
                LOGGER.debug("Token parsing failed", e);
                /* Let the user know to refresh the token. */
                throw new ExpiredJwt();
            }
            if (!(parsedToken instanceof VerifiedAccessToken vat)) {
                /* We received a non-auth token. */
                LOGGER.debug("Non-access token given");
                throw new NoJwtProvided();
            }

            LOGGER.trace("Token accepted");
            final var principal = new AuthenticatedBankUserPrincipal(vat.getRole(), vat.getSub());
            final var auth =
                new AuthenticatedBankUserAuthentication(
                    principal,
                    providedToken.rawJwt(),
                    vat.getPrivileges()
                );
            SecurityContextHolder.getContext()
                .setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    private static final String BEARER_PREFIX = "bearer ";

    private UnverifiedToken parseAuthHeader(String header) {
        if (header == null) return null;
        try {
            if (
                !header.substring(0, BEARER_PREFIX.length())
                    .equalsIgnoreCase(BEARER_PREFIX)
            ) return null;

            final var tokenPart = header.substring(BEARER_PREFIX.length());
            return new UnverifiedToken(tokenPart);
        } catch (IndexOutOfBoundsException ignored) {
            return null;
        }
    }
}
