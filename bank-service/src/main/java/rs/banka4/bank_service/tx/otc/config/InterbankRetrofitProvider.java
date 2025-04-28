package rs.banka4.bank_service.tx.otc.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import org.springframework.stereotype.Service;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import rs.banka4.bank_service.tx.config.InterbankConfig;

@Service
@Slf4j
public class InterbankRetrofitProvider {
    private final Map<Long, OkHttpClient> clients = new HashMap<>();
    private final Map<Long, Retrofit> retrofits = new HashMap<>();
    private final Map<Long, InterbankService> interbanks = new HashMap<>();

    public InterbankRetrofitProvider(InterbankConfig cfg, ObjectMapper objectMapper) {
        for (
            final var route : cfg.getRoutingTable()
                .entrySet()
        ) {
            final var bankId = route.getKey();
            final var bankCfg = route.getValue();

            final var logger = new HttpLoggingInterceptor(m -> log.trace("{}", m));
            logger.setLevel(Level.BODY);
            final var client =
                new OkHttpClient.Builder().addInterceptor(logger)
                    .addInterceptor(
                        chain -> chain.proceed(
                            chain.request()
                                .newBuilder()
                                .header("X-Api-Key", bankCfg.getApiKey())
                                .build()
                        )
                    )
                    .build();
            clients.put(bankId, client);
            final var retrofit =
                new Retrofit.Builder().client(client)
                    .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                    .baseUrl(bankCfg.getBaseUrl())
                    .build();
            retrofits.put(bankId, retrofit);

            interbanks.put(bankId, retrofit.create(InterbankService.class));
        }
    }

    public InterbankService get(long routingNumber) {
        return Optional.ofNullable(interbanks.get(routingNumber))
            .orElseThrow(() -> new IllegalArgumentException("Unknown routing number"));
    }

    public Collection<InterbankService> getAll() {
        return interbanks.values();
    }
}
