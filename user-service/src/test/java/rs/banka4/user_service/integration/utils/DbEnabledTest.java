package rs.banka4.user_service.integration.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Import;

/** A test class annotation that provides a clean production PostgreSQL
 *  database to each test method.  A test marked with this class is also
 *  considered an integration test automatically.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(PSQLTestContainerConfig.class)
@ExtendWith(FlywayCleanerExtension.class)
@IntegrationTest
public @interface DbEnabledTest {
}
