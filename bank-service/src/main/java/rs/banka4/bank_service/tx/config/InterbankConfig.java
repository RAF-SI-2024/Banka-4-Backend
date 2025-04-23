package rs.banka4.bank_service.tx.config;

import java.time.Duration;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@AllArgsConstructor
@Configuration
@ConfigurationProperties("rafeisen.interbank")
public class InterbankConfig {
    private Map<Long, SingleBankConfig> routingTable;

    /**
     * How frequently should messages be re-sent?
     */
    private Duration resendDuration = Duration.ofSeconds(15);

    @Data
    public static class SingleBankConfig {
        private String apiKey;
        private String baseUrl;
    }
}
