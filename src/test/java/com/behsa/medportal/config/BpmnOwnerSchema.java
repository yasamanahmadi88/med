package com.behsa.medportal.config;

import java.sql.ResultSet;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Creates the Owner -&gt; Group -&gt; Element tables that the BPMN access tests seed directly.
 *
 * <p>Every test profile disables Liquibase, and these four tables are reached through native
 * queries rather than mapped entities, so Hibernate's ddl-auto does not produce them either.
 * The same suites run against both H2 and Oracle, so the DDL stays on ANSI types and the
 * existence check goes through JDBC metadata rather than a vendor-specific IF NOT EXISTS.
 */
public final class BpmnOwnerSchema {

    private BpmnOwnerSchema() {}

    public static void ensure(JdbcTemplate jdbcTemplate) {
        create(
            jdbcTemplate,
            "TBL_BPMN_GROUP_ELEMENT",
            """
            CREATE TABLE TBL_BPMN_GROUP_ELEMENT (
                group_key NUMERIC(19) NOT NULL,
                element_key NUMERIC(19) NOT NULL,
                PRIMARY KEY (group_key, element_key)
            )
            """
        );

        create(
            jdbcTemplate,
            "TBL_OWNER_BPMN_GROUP",
            """
            CREATE TABLE TBL_OWNER_BPMN_GROUP (
                owner_key NUMERIC(19) NOT NULL,
                group_key NUMERIC(19) NOT NULL,
                enabled INTEGER DEFAULT 1 NOT NULL,
                PRIMARY KEY (owner_key, group_key)
            )
            """
        );

        create(
            jdbcTemplate,
            "TBL_PORTAL_OWNER",
            """
            CREATE TABLE TBL_PORTAL_OWNER (
                owner_key NUMERIC(19) NOT NULL PRIMARY KEY,
                owner_code VARCHAR(50) NOT NULL,
                owner_name VARCHAR(100) NOT NULL,
                display_name VARCHAR(150) NOT NULL,
                description VARCHAR(500),
                enabled INTEGER DEFAULT 1 NOT NULL
            )
            """
        );

        create(
            jdbcTemplate,
            "TBL_PORTAL_CONFIGURATION",
            """
            CREATE TABLE TBL_PORTAL_CONFIGURATION (
                config_key NUMERIC(19) NOT NULL PRIMARY KEY,
                active_owner_key NUMERIC(19) NOT NULL
            )
            """
        );
    }

    private static void create(JdbcTemplate jdbcTemplate, String table, String ddl) {
        if (!exists(jdbcTemplate, table)) {
            jdbcTemplate.execute(ddl);
        }
    }

    private static boolean exists(JdbcTemplate jdbcTemplate, String table) {
        return Boolean.TRUE.equals(
            jdbcTemplate.execute((ConnectionCallback<Boolean>) connection -> {
                try (ResultSet tables = connection.getMetaData().getTables(null, null, table, null)) {
                    return tables.next();
                }
            })
        );
    }
}
