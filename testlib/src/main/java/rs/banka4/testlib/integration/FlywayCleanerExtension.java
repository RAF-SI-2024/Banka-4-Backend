package rs.banka4.testlib.integration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/** JUnit Jupiter extension that automatically runs a Flyway clean and
 *  re-migration before each test.
 */
public class FlywayCleanerExtension implements BeforeEachCallback {
    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        var ctx = SpringExtension.getApplicationContext(context);
        var flyway = ctx.getBean(Flyway.class);

        /* Relies on application.yml setting spring.flyway.clean-disabled.  */
        flyway.clean();
        flyway.migrate();
    }
}
