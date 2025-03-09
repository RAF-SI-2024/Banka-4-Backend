package rs.banka4.user_service.integration.utils;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@TestConfiguration
public class PSQLTestContainerConfig {
    @Container
    private static final PostgreSQLContainer<?> postgres
        = new PostgreSQLContainer<>("postgres:17");

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgres() {
        return postgres;
    }
}
