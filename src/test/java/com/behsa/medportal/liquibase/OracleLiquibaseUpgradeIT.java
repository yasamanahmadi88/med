package com.behsa.medportal.liquibase;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Upgrade path:
 * <ol>
 *   <li>Empty Oracle Free</li>
 *   <li>Deploy legacy initial schema only (name PK authority model)</li>
 *   <li>Run current master.xml forward migrations</li>
 *   <li>Assert users/roles preserved and schema uses authority_id</li>
 *   <li>Re-run Liquibase for idempotency</li>
 * </ol>
 *
 * <p>Sanitized baseline SQL remains at {@code oracle/legacy-jhipster-authority-baseline.sql}
 * for manual replay / documentation.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OracleLiquibaseUpgradeIT {

    private static OracleContainer oracle;
    private static String jdbcUrl;
    private static String username;
    private static String password;

    @BeforeAll
    static void startOracle() {
        String image = System.getProperty(
            "testcontainers.oracle.image",
            System.getenv().getOrDefault("TESTCONTAINERS_ORACLE_IMAGE", "gvenzl/oracle-free:slim")
        );
        oracle = new OracleContainer(DockerImageName.parse(image))
            .withUsername("MEDIATION")
            .withPassword("MEDIATION");
        oracle.start();
        jdbcUrl = oracle.getJdbcUrl();
        username = oracle.getUsername();
        password = oracle.getPassword();
    }

    @AfterAll
    static void stopOracle() {
        if (oracle != null) {
            oracle.stop();
        }
    }

    @Test
    @Order(1)
    void legacyInitialSchemaUsesNamePrimaryKey() throws Exception {
        runLiquibase("config/liquibase/legacy-only-master.xml", null);

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password); Statement statement = connection.createStatement()) {
            try (
                ResultSet rs = statement.executeQuery(
                    "SELECT COUNT(*) FROM USER_TAB_COLUMNS WHERE TABLE_NAME='JHI_AUTHORITY' AND COLUMN_NAME='NAME'"
                )
            ) {
                rs.next();
                assertThat(rs.getInt(1)).isEqualTo(1);
            }
            try (
                ResultSet rs = statement.executeQuery(
                    "SELECT COUNT(*) FROM USER_TAB_COLUMNS WHERE TABLE_NAME='JHI_AUTHORITY' AND COLUMN_NAME='ID'"
                )
            ) {
                rs.next();
                assertThat(rs.getInt(1)).isZero();
            }
            try (ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM jhi_user")) {
                rs.next();
                assertThat(rs.getInt(1)).isEqualTo(2);
            }
            try (
                ResultSet rs = statement.executeQuery(
                    "SELECT authority_name FROM jhi_user_authority WHERE user_id = 1 ORDER BY authority_name"
                )
            ) {
                List<String> roles = new ArrayList<>();
                while (rs.next()) {
                    roles.add(rs.getString(1));
                }
                assertThat(roles).containsExactly("ROLE_ADMIN", "ROLE_USER");
            }
        }
    }

    @Test
    @Order(2)
    void masterChangelogMigratesToNumericAuthorityAndKeepsUsers() throws Exception {
        runLiquibase("config/liquibase/master.xml", "test");

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password); Statement statement = connection.createStatement()) {
            try (
                ResultSet rs = statement.executeQuery(
                    "SELECT COUNT(*) FROM USER_TAB_COLUMNS WHERE TABLE_NAME='JHI_AUTHORITY' AND COLUMN_NAME='ID'"
                )
            ) {
                rs.next();
                assertThat(rs.getInt(1)).isEqualTo(1);
            }
            try (
                ResultSet rs = statement.executeQuery(
                    "SELECT COUNT(*) FROM USER_TAB_COLUMNS WHERE TABLE_NAME='JHI_USER_AUTHORITY' AND COLUMN_NAME='AUTHORITY_NAME'"
                )
            ) {
                rs.next();
                assertThat(rs.getInt(1)).isZero();
            }
            try (ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM jhi_user WHERE login IN ('admin','user')")) {
                rs.next();
                assertThat(rs.getInt(1)).isEqualTo(2);
            }
            try (
                ResultSet rs = statement.executeQuery(
                    """
                    SELECT a.name FROM jhi_user u
                    JOIN jhi_user_authority ua ON ua.user_id = u.id
                    JOIN jhi_authority a ON a.id = ua.authority_id
                    WHERE u.login = 'admin'
                    ORDER BY a.name
                    """
                )
            ) {
                List<String> roles = new ArrayList<>();
                while (rs.next()) {
                    roles.add(rs.getString(1));
                }
                assertThat(roles).contains("ROLE_ADMIN", "ROLE_USER");
            }
            try (ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM jhi_resource")) {
                rs.next();
                assertThat(rs.getInt(1)).isGreaterThan(0);
            }
            try (ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM tbl_modules")) {
                rs.next();
                assertThat(rs.getInt(1)).isZero();
            }
        }
    }

    @Test
    @Order(3)
    void secondMasterRunIsIdempotent() throws Exception {
        List<String> before = changeSetIds();
        runLiquibase("config/liquibase/master.xml", "test");
        assertThat(changeSetIds()).containsExactlyElementsOf(before);
    }

    private static void runLiquibase(String changeLog, String contexts) throws Exception {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            Liquibase liquibase = new Liquibase(
                changeLog,
                new ClassLoaderResourceAccessor(),
                DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection))
            );
            if (contexts == null || contexts.isBlank()) {
                liquibase.update("");
            } else {
                liquibase.update(contexts);
            }
        }
    }

    private static List<String> changeSetIds() throws Exception {
        List<String> ids = new ArrayList<>();
        try (
            Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT ID FROM DATABASECHANGELOG ORDER BY ORDEREXECUTED")
        ) {
            while (rs.next()) {
                ids.add(rs.getString(1));
            }
        }
        return ids;
    }
}
