package rs.banka4.user_service.config.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import rs.banka4.user_service.config.WhiteListConfig;
import rs.banka4.user_service.exceptions.jwt.NoJwtProvided;
import rs.banka4.user_service.service.impl.CustomUserDetailsService;
import rs.banka4.user_service.utils.JwtUtil;


/**
 * Filter that checks if the request has a valid JWT token in the Authorization header.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtils;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtils, CustomUserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

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

        final String authorizationHeader = request.getHeader("Authorization");
        String username = null;
        String token = null;

        if (!WhiteListConfig.isWhitelisted(request.getRequestURI())) {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                token = authorizationHeader.substring(7);
                username = jwtUtils.extractUsername(token);
            } else {
                throw new NoJwtProvided();
            }
        }

        if (
            username != null
                && SecurityContextHolder.getContext()
                    .getAuthentication()
                    == null
        ) {
            if (jwtUtils.validateToken(token, username)) {
                String role = jwtUtils.extractRole(token);
                if (Objects.equals(role, "client")) {
                    CustomUserDetailsService.role = "client";
                } else {
                    CustomUserDetailsService.role = "employee";
                }

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        username,
                        token,
                        userDetailsService.loadUserByUsername(username)
                            .getAuthorities()
                    );
                authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );

                CustomUserDetailsService.role = "";
                SecurityContextHolder.getContext()
                    .setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
