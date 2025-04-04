package rs.banka4.stock_service.config.retrofit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Configuration
public class RetroFitConfig {

    @Bean
    public AlphaVantageService alphaRetrofit(ObjectMapper objectMapper) {
        Retrofit retrofit =
            new Retrofit.Builder().baseUrl("https://www.alphavantage.co/")
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .build();

        return retrofit.create(AlphaVantageService.class);
    }
}
