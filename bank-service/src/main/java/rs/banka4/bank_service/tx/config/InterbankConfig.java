package rs.banka4.bank_service.tx.config;

import java.time.Duration;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NoArgsConstructor
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
