package com.behsa.medportal.config;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Shared Oracle Free Testcontainers wrapper.
 *
 * <p>Integration tests use {@code @DirtiesContext(AFTER_CLASS)}, which recreates the Spring
 * context for every IT class. Starting a fresh Oracle Free container each time is too slow for CI
 * and was causing the suite to be canceled mid-run. The Docker container is therefore held in a
 * JVM-scoped singleton and is not stopped when individual Spring contexts are destroyed.
 */
public class OracleSqlTestContainer implements SqlTestContainer {

    static final String DEFAULT_IMAGE = "gvenzl/oracle-free:slim";
    private static final String ORACLE_IMAGE_PROPERTY = "testcontainers.oracle.image";
    private static final String ORACLE_IMAGE_ENV = "TESTCONTAINERS_ORACLE_IMAGE";

    private static final OracleContainer SHARED_CONTAINER = createSharedContainer();

    private static OracleContainer createSharedContainer() {
        String image = System.getProperty(
            ORACLE_IMAGE_PROPERTY,
            System.getenv().getOrDefault(ORACLE_IMAGE_ENV, DEFAULT_IMAGE)
        );
        return new OracleContainer(DockerImageName.parse(image))
            .withUsername("MEDIATION")
            .withPassword("MEDIATION")
            .withReuse(false);
    }

    @Override
    public void afterPropertiesSet() {
        synchronized (OracleSqlTestContainer.class) {
            if (!SHARED_CONTAINER.isRunning()) {
                SHARED_CONTAINER.start();
            }
        }
    }

    @Override
    public void destroy() {
        // Intentionally keep the shared Oracle container running across DirtiesContext restarts.
        // Ryuk / JVM shutdown cleans it up at the end of the Surefire/Failsafe JVM.
    }

    @Override
    public JdbcDatabaseContainer<?> getTestContainer() {
        return SHARED_CONTAINER;
    }
}
