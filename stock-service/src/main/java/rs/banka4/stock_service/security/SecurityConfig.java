package rs.banka4.stock_service.security;

import static org.springframework.security.config.Customizer.*;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final BearerTokenParsingFilter bearerParsingFilter;

    @Bean
    public SecurityFilterChain configureSecurity(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity.csrf(CsrfConfigurer::disable)
            .cors(withDefaults())
            .authorizeHttpRequests(
                request -> request.requestMatchers("/docs/api")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/listings/**")
                    .authenticated()
                    .anyRequest()
                    .authenticated()
            )
            .sessionManagement(ses -> ses.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            /* We don't use username:password authentication in this household. */
            .addFilterAfter(bearerParsingFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public AuthenticationManager noopAuthenticationManager() {
        return authentication -> {
            throw new AuthenticationServiceException(
                "This service cannot deal with users, just tokens"
            );
        };
    }
}
