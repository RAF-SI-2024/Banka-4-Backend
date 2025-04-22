package rs.banka4.bank_service.tx.config;

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

    @Data
    public static class SingleBankConfig {
        private String apiKey;
        private String baseUrl;
    }
}
