package com.behsa.medportal;

import com.behsa.medportal.config.AsyncSyncConfiguration;
import com.behsa.medportal.config.EmbeddedSQL;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

/**
 * Base composite annotation for integration tests.
 *
 * <p>Active Spring profiles come from Failsafe {@code -Dspring.profiles.active=${profile.test}}
 * (default Maven {@code dev} → {@code test,testdev}; {@code -Poracle-testcontainers} →
 * {@code test,testcontainers}).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(classes = { MedPortalApp.class, AsyncSyncConfiguration.class })
@EmbeddedSQL
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public @interface IntegrationTest {
}
