package com.behsa.medportal.config;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Profiles;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;

public class TestContainersSpringContextCustomizerFactory implements ContextCustomizerFactory {

    private Logger log = LoggerFactory.getLogger(TestContainersSpringContextCustomizerFactory.class);

    /** One wrapper per JVM so DirtiesContext does not restart Oracle Free repeatedly. */
    private static final OracleSqlTestContainer SHARED_ORACLE = new OracleSqlTestContainer();

    @Override
    public ContextCustomizer createContextCustomizer(Class<?> testClass, List<ContextConfigurationAttributes> configAttributes) {
        return (context, mergedConfig) -> {
            ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
            TestPropertyValues testValues = TestPropertyValues.empty();
            EmbeddedSQL sqlAnnotation = AnnotatedElementUtils.findMergedAnnotation(testClass, EmbeddedSQL.class);
            if (null != sqlAnnotation) {
                log.debug("detected the EmbeddedSQL annotation on class {}", testClass.getName());
                log.info("Warming up the sql database");
            }
            if (null != sqlAnnotation && context.getEnvironment().acceptsProfiles(Profiles.of("testcontainers"))) {
                SHARED_ORACLE.afterPropertiesSet();
                JdbcDatabaseContainer<?> container = SHARED_ORACLE.getTestContainer();
                if (!beanFactory.containsSingleton("oracleSqlTestContainer")) {
                    beanFactory.registerSingleton("oracleSqlTestContainer", SHARED_ORACLE);
                }
                testValues = TestPropertyValues.of(
                    "spring.datasource.url=" + container.getJdbcUrl(),
                    "spring.datasource.username=" + container.getUsername(),
                    "spring.datasource.password=" + container.getPassword(),
                    "spring.datasource.driver-class-name=oracle.jdbc.OracleDriver",
                    "spring.jpa.database-platform=org.hibernate.dialect.OracleDialect",
                    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.OracleDialect"
                );
            }
            testValues.applyTo(context);
        };
    }
}
