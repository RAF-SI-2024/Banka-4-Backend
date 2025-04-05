package rs.banka4.stock_service.config;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Configuration
public class RetrofitConfig {

    @Bean
    public Retrofit userServiceRetrofit() {
        return createRetrofit("http://user_service:8080");
    }

    private Retrofit createRetrofit(String baseUrl) {
        OkHttpClient client = new OkHttpClient.Builder().build();

        return new Retrofit.Builder().baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(JacksonConverterFactory.create())
            .build();
    }
}
