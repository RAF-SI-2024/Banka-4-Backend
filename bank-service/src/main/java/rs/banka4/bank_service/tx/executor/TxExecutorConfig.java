package rs.banka4.bank_service.tx.executor;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/** Beans used by the transaction executor. */
@Configuration
public class TxExecutorConfig {
    @Bean
    public Executor txExecutorPool() {
        final var executor = new ThreadPoolTaskExecutor();
        /*
         * We won't need more threads, since all TX executors are synchronized anyway.
         */
        executor.setCorePoolSize(1);
        executor.setThreadNamePrefix("TX-");
        return executor;
    }
}
