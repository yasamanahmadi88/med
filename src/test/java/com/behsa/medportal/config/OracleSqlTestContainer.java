package com.behsa.medportal.config;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.utility.DockerImageName;

public class OracleSqlTestContainer implements SqlTestContainer {

    static final String DEFAULT_IMAGE = "gvenzl/oracle-free:slim";
    private static final String ORACLE_IMAGE_PROPERTY = "testcontainers.oracle.image";
    private static final String ORACLE_IMAGE_ENV = "TESTCONTAINERS_ORACLE_IMAGE";

    private final OracleContainer oracleContainer;

    public OracleSqlTestContainer() {
        String image = System.getProperty(
            ORACLE_IMAGE_PROPERTY,
            System.getenv().getOrDefault(ORACLE_IMAGE_ENV, DEFAULT_IMAGE)
        );
        oracleContainer = new OracleContainer(DockerImageName.parse(image))
            .withUsername("MEDIATION")
            .withPassword("MEDIATION")
            .withReuse(true);
    }

    @Override
    public void afterPropertiesSet() {
        if (!oracleContainer.isRunning()) {
            oracleContainer.start();
        }
    }

    @Override
    public void destroy() {
        if (oracleContainer.isRunning()) {
            oracleContainer.stop();
        }
    }

    @Override
    public JdbcDatabaseContainer<?> getTestContainer() {
        return oracleContainer;
    }
}
