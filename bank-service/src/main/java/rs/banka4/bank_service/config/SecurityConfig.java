package rs.banka4.bank_service.config;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import rs.banka4.bank_service.config.filters.*;
import rs.banka4.bank_service.security.InterbankAuthentication;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final InterbankAuthFilter interbankAuthFilter;
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final RateLimitingFilter rateLimitingFilter;
    private final ExceptionHandlingFilter exceptionHandlingFilter;
    private final AuthenticationProvider authenticationProvider;
    private final InvalidRouteFilter invalidRouteFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(
                auth -> auth.requestMatchers(WhiteListConfig.WHITE_LIST_URL)
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/employee/search")
                    .access(
                        AuthorizationManagers.allOf(
                            AuthorityAuthorizationManager.hasAuthority("EMPLOYEE"),
                            AuthorityAuthorizationManager.hasAuthority("ADMIN")
                        )
                    )
                    .requestMatchers(HttpMethod.GET, "/employee/search/actuary-only")
                    .access(
                        AuthorizationManagers.anyOf(
                            AuthorityAuthorizationManager.hasAuthority("SUPERVISOR"),
                            AuthorityAuthorizationManager.hasAuthority("ADMIN")
                        )
                    )
                    .requestMatchers(HttpMethod.GET, "/employee/privileges")
                    .hasAuthority("EMPLOYEE")
                    .requestMatchers(HttpMethod.POST, "/employee")
                    .hasAuthority("EMPLOYEE")
                    .requestMatchers(HttpMethod.PUT, "/employee/{id}")
                    .hasAuthority("EMPLOYEE")
                    .requestMatchers(HttpMethod.GET, "/employee/{id}")
                    .hasAuthority("EMPLOYEE")
                    .requestMatchers(HttpMethod.GET, "/account/bank-accounts")
                    .hasAuthority("EMPLOYEE")
                    /* Previously stock service. */
                    .requestMatchers(HttpMethod.GET, "/stock/listings/**")
                    .authenticated()
                    .requestMatchers(HttpMethod.PUT, "/stock/actuaries/limit/**")
                    .access(
                        AuthorizationManagers.anyOf(
                            AuthorityAuthorizationManager.hasAuthority("SUPERVISOR"),
                            AuthorityAuthorizationManager.hasAuthority("ADMIN")
                        )
                    )
                    .requestMatchers(HttpMethod.GET, "/stock/actuaries/search")
                    .access(
                        AuthorizationManagers.anyOf(
                            AuthorityAuthorizationManager.hasAuthority("SUPERVISOR"),
                            AuthorityAuthorizationManager.hasAuthority("ADMIN")
                        )
                    )
                    .requestMatchers(
                        HttpMethod.GET,
                        "/stock/orders",
                        "/stock/orders/*",
                        "/stock/orders/{orderId}/approve",
                        "/stock/orders/{orderId}/decline"
                    )
                    .access(
                        AuthorizationManagers.anyOf(
                            AuthorityAuthorizationManager.hasAuthority("SUPERVISOR"),
                            AuthorityAuthorizationManager.hasAuthority("ADMIN")
                        )
                    )
                    .requestMatchers(HttpMethod.POST, "/stock/actuaries/register")
                    .hasAuthority("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/stock/actuaries/update/**")
                    .hasAuthority("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/stock/tax/trigger-monthly")
                    .access(
                        AuthorizationManagers.anyOf(
                            AuthorityAuthorizationManager.hasAuthority("SUPERVISOR"),
                            AuthorityAuthorizationManager.hasAuthority("ADMIN")
                        )
                    )
                    .requestMatchers(HttpMethod.POST, "/stock/tax/collect/**")
                    .access(
                        AuthorizationManagers.anyOf(
                            AuthorityAuthorizationManager.hasAuthority("SUPERVISOR"),
                            AuthorityAuthorizationManager.hasAuthority("ADMIN")
                        )
                    )
                    .requestMatchers(HttpMethod.POST, "/stock/orders", "/stock/orders/")
                    .access(
                        AuthorizationManagers.anyOf(
                            AuthorityAuthorizationManager.hasAuthority("EMPLOYEE"),
                            AuthorityAuthorizationManager.hasAuthority("TRADE"),
                            AuthorityAuthorizationManager.hasAuthority("ADMIN")
                        )
                    )
                    .requestMatchers(HttpMethod.GET, "/stock/tax/summary")
                    .access(
                        AuthorizationManagers.anyOf(
                            AuthorityAuthorizationManager.hasAuthority("SUPERVISOR"),
                            AuthorityAuthorizationManager.hasAuthority("ADMIN")
                        )
                    )
                    .requestMatchers(HttpMethod.GET, "/stock/stock/securities/bank/profit")
                    .access(
                        AuthorizationManagers.anyOf(
                            AuthorityAuthorizationManager.hasAuthority("SUPERVISOR"),
                            AuthorityAuthorizationManager.hasAuthority("ADMIN")
                        )
                    )
                    /* All inter-bank routes. */
                    .requestMatchers("/interbank/**")
                    .hasAuthority(InterbankAuthentication.OTHER_BANK_AUTHORITY)
                    /* Global fallback. */
                    .anyRequest()
                    .authenticated()
            )
            .sessionManagement(ses -> ses.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(exceptionHandlingFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(invalidRouteFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(interbankAuthFilter, JwtAuthenticationFilter.class);

        return httpSecurity.build();
    }

    @Bean
    @Profile("dev")
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        configuration.setAllowedMethods(
            List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        );
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
