package rs.banka4.user_service.integration.utils;

import org.junit.jupiter.api.Tag;

/** Marks a test as an integration tests.  Use for all tests that check the
 *  don't mock anything between the Spring controllers and the database, except
 *  for perhaps other microservices.
 */
@Tag("integration")
public @interface IntegrationTest {

}
