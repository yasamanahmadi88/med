package com.behsa.medportal.config;

import com.behsa.medportal.service.ResourceAuthorityQueryService;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.method.AuthorizationManagerBeforeMethodInterceptor;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.aop.Advisor;
import org.springframework.security.authorization.method.AuthorizationInterceptorsOrder;

/**
 * Method security for MedPortal.
 *
 * <p>Uses a custom {@link AuthorizationManager} so {@code @Secured("resourceName")} continues to
 * enforce resource+verb checks (legacy {@code CustomAccessDecisionManager} behavior) on Spring
 * Security 6+/7, while {@code @Secured("ROLE_*")} keeps standard role checks.
 *
 * <p>{@code securedEnabled} stays false to avoid registering Spring's default Secured advisor
 * alongside this custom one (double evaluation). The custom advisor below is the sole @Secured enforcer.
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = false)
public class MethodSecurityConfiguration {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    Advisor securedResourceAuthorizationAdvisor(ResourceAuthorityQueryService resourceAuthorityQueryService) {
        AuthorizationManager<MethodInvocation> manager = new ResourceSecuredAuthorizationManager(resourceAuthorityQueryService);
        AuthorizationManagerBeforeMethodInterceptor interceptor = AuthorizationManagerBeforeMethodInterceptor.secured(manager);
        interceptor.setOrder(AuthorizationInterceptorsOrder.SECURED.getOrder());
        return interceptor;
    }
}
