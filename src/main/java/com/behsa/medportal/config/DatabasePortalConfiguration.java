package com.behsa.medportal.config;

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Primary datasource / EMF wiring retained from MedPortal baseline.
 *
 * <p>Updated for Spring Boot 4 package moves:
 * {@code DataSourceProperties} → {@code org.springframework.boot.jdbc.autoconfigure},
 * {@code EntityManagerFactoryBuilder} → {@code org.springframework.boot.jpa}.
 */
@Configuration
@EnableTransactionManagement
public class DatabasePortalConfiguration {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties defaultDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource defaultDataSource() {
        return defaultDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean(name = "entityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean customerEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
            .dataSource(defaultDataSource())
            .packages("com.behsa.medportal.domain", "com.behsa.medportal.repository.timezone")
            .persistenceUnit("default")
            .build();
    }

    @Bean(name = "transactionManager")
    @Primary
    public JpaTransactionManager db2TransactionManager(@Qualifier("entityManagerFactory") final EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }
}
