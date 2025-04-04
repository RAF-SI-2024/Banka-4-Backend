package rs.banka4.stock_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rs.banka4.rafeisen.common.exceptions.ErrorResponseHandler;

@Configuration
public class ApplicationConfig {
    /**
     * Provide an handler for errors in responses.
     */
    @Bean
    public ErrorResponseHandler errorResponseHandler() {
        return new ErrorResponseHandler();
    }
}
