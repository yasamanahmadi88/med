package com.behsa.medportal.med;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * https://stackoverflow.com/questions/46965253/jhipster-configure-multiple-datasources/47756454
 * */
@Configuration
@EnableTransactionManagement
@EntityScan(basePackages = "icom.behsa.medportal.med.domain")
@EnableJpaRepositories(
    transactionManagerRef = "medTransactionManager",
    entityManagerFactoryRef = "medEntityManagerFactory",
    basePackages = "com.behsa.medportal.med.repository"
)
public class DatabaseMedConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "med.datasource")
    public DataSourceProperties ocsDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "med.datasource")
    public DataSource ocsDataSource() {
        return ocsDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean(name = "medEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean customerEntityManagerFactory(
        EntityManagerFactoryBuilder builder) {
        Properties properties = new Properties();

        LocalContainerEntityManagerFactoryBean emf = builder
            .dataSource(ocsDataSource())
            .packages("com.behsa.medportal.med")
            .persistenceUnit("med")
            .build();
        emf.setJpaProperties(properties);
        return emf;
    }

    @Bean(name = "medTransactionManager")
    public JpaTransactionManager db2TransactionManager(@Qualifier("medEntityManagerFactory") final EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }

}
