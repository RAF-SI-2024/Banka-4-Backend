package rs.banka4.user_service.integration.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;

/** Marks a test as an integration tests.  Use for all tests that check the
 *  don't mock anything between the Spring controllers and the database, except
 *  for perhaps other microservices.
 */
@Tag("integration")
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface IntegrationTest {

}
