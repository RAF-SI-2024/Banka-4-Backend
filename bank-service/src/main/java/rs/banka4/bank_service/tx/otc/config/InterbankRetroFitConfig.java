package rs.banka4.bank_service.tx.otc.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import rs.banka4.bank_service.tx.config.InterbankConfig;

@Configuration
@RequiredArgsConstructor
public class InterbankRetroFitConfig {
    private final InterbankConfig interbankConfig;

    @Bean
    public InterbankService interbankService(ObjectMapper objectMapper) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        httpClient.addInterceptor(chain -> {
            Request request =
                chain.request()
                    .newBuilder()
                    .addHeader(
                        "x-apiKey",
                        interbankConfig.getRoutingTable()
                            .get(111l)
                            .getApiKey()
                    )
                    .build();
            return chain.proceed(request);
        });
        Retrofit retrofit =
            new Retrofit.Builder().baseUrl(
                interbankConfig.getRoutingTable()
                    .get(111l)
                    .getBaseUrl()
            )
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .client(httpClient.build())
                .build();

        return retrofit.create(InterbankService.class);
    }
}
