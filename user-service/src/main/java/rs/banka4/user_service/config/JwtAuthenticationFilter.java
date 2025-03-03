package rs.banka4.user_service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import rs.banka4.user_service.exceptions.jwt.NoJwtProvided;
import rs.banka4.user_service.service.impl.CustomUserDetailsService;
import rs.banka4.user_service.utils.JwtUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtils;
    private final CustomUserDetailsService userDetailsService;
    private final List<Pattern> WHITE_LIST_URL_PATTERNS = List.of(
            Pattern.compile("/docs/.*")
    );

    public JwtAuthenticationFilter(JwtUtil jwtUtils, CustomUserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");
        String username = null;
        String token = null;

        boolean isWhitelisted = Arrays.stream(SecurityConfig.WHITE_LIST_URL)
                .anyMatch(url -> request.getRequestURI().startsWith(url)) ||
                WHITE_LIST_URL_PATTERNS.stream()
                        .anyMatch(pattern -> pattern.matcher(request.getRequestURI()).matches());

        if (!isWhitelisted) {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                token = authorizationHeader.substring(7);
                username = jwtUtils.extractUsername(token);
            } else {
                throw new NoJwtProvided();
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtils.validateToken(token, username)) {
                String role = jwtUtils.extractRole(token);
                if (Objects.equals(role, "client")) {
                    CustomUserDetailsService.role = "client";
                } else {
                    CustomUserDetailsService.role = "employee";
                }

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username, token, userDetailsService.loadUserByUsername(username).getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                CustomUserDetailsService.role = "";
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}