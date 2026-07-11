package com.behsa.medportal.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Seeds IT fixture data on Oracle Testcontainers after Hibernate has created
 * entity tables (Boot sql.init runs too early against a Liquibase-only schema).
 */
@Component
@Profile("testcontainers")
@Order(100)
public class OracleTestDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(OracleTestDataLoader.class);

    private final DataSource dataSource;

    public OracleTestDataLoader(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Loading Oracle IT seed data from classpath:test-data.sql");
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("test-data.sql"));
        populator.setContinueOnError(false);
        populator.execute(dataSource);
    }
}
