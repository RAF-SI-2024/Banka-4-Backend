package rs.banka4.bank_service.tx.config;

import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NoArgsConstructor
@ConfigurationProperties("rafeisen.interbank")
@Valid
public class InterbankConfig {
    @Valid
    @Nonnull
    private Map<Long, SingleBankConfig> routingTable = new HashMap<>();

    /**
     * How frequently should messages be re-sent?
     */
    private Duration resendDuration = Duration.ofSeconds(15);

    @Data
    @Valid
    public static class SingleBankConfig {
        @Nonnull
        private String apiKey;
        @Nonnull
        private String baseUrl;
        @Nonnull
        private String receptionKey;
    }

    /**
     * Given an API key {@code apiKey}, check which bank is the sender.
     *
     * @return Sending bank routing number, if found.
     */
    public Optional<Long> resolveApiKey(String apiKey) {
        return routingTable.entrySet()
            .stream()
            .filter(
                e -> e.getValue()
                    .getReceptionKey()
                    .equals(apiKey)
            )
            .map(Map.Entry::getKey)
            .findAny();
    }
}
