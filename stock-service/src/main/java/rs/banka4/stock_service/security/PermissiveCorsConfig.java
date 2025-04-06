package rs.banka4.stock_service.security;

import io.swagger.v3.oas.models.PathItem.HttpMethod;
import java.util.stream.Stream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Profile("dev")
@Configuration
public class PermissiveCorsConfig {
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        final var config = new CorsConfiguration();
        config.applyPermitDefaultValues()
            .setAllowedMethods(
                Stream.of(HttpMethod.values())
                    .map(Object::toString)
                    .toList()
            );
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
