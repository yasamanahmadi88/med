package com.behsa.medportal.config;

import com.behsa.medportal.service.ResourceAuthorityQueryService;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

/**
 * Custom method security configuration.
 *
 * Enables:
 * - @PreAuthorize / @PostAuthorize
 * - @Secured
 *
 * Uses Spring Security's default AccessDecisionManager first,
 * then applies the project's resource-based permission checks.
 */
@Configuration
@EnableGlobalMethodSecurity(
    prePostEnabled = true,
    securedEnabled = true,
    proxyTargetClass = true,
    mode = AdviceMode.PROXY
)
public class MethodSecurityConfiguration extends GlobalMethodSecurityConfiguration {

    private final ResourceAuthorityQueryService resourceAuthorityQueryService;

    public MethodSecurityConfiguration(ResourceAuthorityQueryService resourceAuthorityQueryService) {
        this.resourceAuthorityQueryService = resourceAuthorityQueryService;
    }

    @Override
    protected AccessDecisionManager accessDecisionManager() {
        return new CustomAccessDecisionManager(
            super.accessDecisionManager(),
            resourceAuthorityQueryService
        );
    }
}
