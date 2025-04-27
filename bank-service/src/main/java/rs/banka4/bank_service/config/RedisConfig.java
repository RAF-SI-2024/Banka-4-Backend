package rs.banka4.bank_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
/* It's late. I won't need it in tests. */
@Profile("!test")
public class RedisConfig {
    private final String cacheHost;
    private final int cachePort;

    public RedisConfig(
        @Value("${rafeisen.cache.valkey.host}") String cacheHost,
        @Value("${rafeisen.cache.valkey.port}") int cachePort
    ) {
        this.cacheHost = cacheHost;
        this.cachePort = cachePort;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(cacheHost, cachePort);
    }
}
