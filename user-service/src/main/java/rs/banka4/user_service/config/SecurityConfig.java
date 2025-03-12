package rs.banka4.user_service.config;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import rs.banka4.user_service.config.filters.ExceptionHandlingFilter;
import rs.banka4.user_service.config.filters.InvalidRouteFilter;
import rs.banka4.user_service.config.filters.JwtAuthenticationFilter;
import rs.banka4.user_service.config.filters.RateLimitingFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final RateLimitingFilter rateLimitingFilter;
    private final ExceptionHandlingFilter exceptionHandlingFilter;
    private final AuthenticationProvider authenticationProvider;
    private final InvalidRouteFilter invalidRouteFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity.csrf(AbstractHttpConfigurer::disable)
            .cors((cors) -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(
                auth -> auth.requestMatchers(WhiteListConfig.WHITE_LIST_URL)
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/employee/search")
                    .hasAuthority("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/employee/privileges")
                    .hasAuthority("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/employee")
                    .hasAuthority("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/employee/{id}")
                    .hasAuthority("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/employee/{id}")
                    .hasAuthority("ADMIN")
                    .anyRequest()
                    .authenticated()
            )
            .sessionManagement(ses -> ses.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(exceptionHandlingFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(invalidRouteFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
