package rs.banka4.stock_service.config;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OkHttpConfig {
    @Bean
    public OkHttpClient stockHttpClient() {
        return new OkHttpClient().newBuilder()
            .addInterceptor(new StockIntereceptor())
            .build();
    }
}
